package br.ufscar.streaming.controller;

import br.ufscar.streaming.domain.*;
import br.ufscar.streaming.repository.*;
import br.ufscar.streaming.service.AudioStorage;
import br.ufscar.streaming.service.CatalogImages;
import br.ufscar.streaming.service.PlatformService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador REST que provê endpoints assíncronos para a camada de visão Thymeleaf,
 * garantindo que todas as interações ocorram sem interromper o reprodutor de áudio (RNF001).
 * Implementa os fluxos dos casos de uso UC01 a UC09.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@SuppressWarnings("null")
public class ApiController {
    private static final String SESSION_USER = "userId";

    private final UserRepository userRepository;
    private final ArtistProfileRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final AudioTrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final PlayHistoryRepository historyRepository;
    private final AbuseReportRepository reportRepository;
    private final AuditLogRepository auditRepository;
    private final PlatformService platformService;
    private final AudioStorage audioStorage;
    private final TransactionTemplate transactions;
    private final CatalogImages images;

    public ApiController(UserRepository userRepository,
                         ArtistProfileRepository artistRepository,
                         AlbumRepository albumRepository,
                         AudioTrackRepository trackRepository,
                         PlaylistRepository playlistRepository,
                         PlayHistoryRepository historyRepository,
                         AbuseReportRepository reportRepository,
                         AuditLogRepository auditRepository,
                         PlatformService platformService,
                         AudioStorage audioStorage,
                         TransactionTemplate transactions,
                         CatalogImages images) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
        this.historyRepository = historyRepository;
        this.reportRepository = reportRepository;
        this.auditRepository = auditRepository;
        this.platformService = platformService;
        this.audioStorage = audioStorage;
        this.transactions = transactions;
        this.images = images;
    }

    /**
     * Retorna o estado sincronizado das entidades persistidas para inicialização da interface.
     *
     * @return mapa contendo usuários, artistas, álbuns, faixas, playlists, histórico, denúncias e logs
     */
    @GetMapping("/bootstrap")
    public Map<String, Object> bootstrap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("users", userRepository.findAll());
        data.put("artists", artistRepository.findAll());
        data.put("albums", albumRepository.findAll());
        data.put("tracks", trackRepository.findAll());
        data.put("playlists", playlistRepository.findAll());
        data.put("history", historyRepository.findAll());
        data.put("reports", reportRepository.findAllByOrderByCreatedAtDesc());
        data.put("auditLogs", auditRepository.findAllByOrderByTimestampDesc());
        return data;
    }

    /**
     * Registra uma nova conta de usuário garantindo unicidade de e-mail e hash BCrypt (RF001, UC06).
     *
     * @param body dados cadastrais enviados pelo cliente
     * @return resposta HTTP 200 com o usuário criado ou HTTP 409 em caso de duplicidade de e-mail
     */
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String fullName = body.getOrDefault("fullName", "").trim();
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");
        String roleStr = body.getOrDefault("role", "OUVINTE").toUpperCase();

        if (fullName.isEmpty() || email.isEmpty() || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Preencha nome completo, e-mail válido e senha com ao menos 6 caracteres."));
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "O e-mail informado já está vinculado a outro cadastro (UC06)."));
        }

        Role role = "CRIADOR".equals(roleStr) ? Role.CRIADOR : Role.OUVINTE;
        User user = new User(fullName, email, platformService.hashPassword(password), role,
                "Membro da comunidade de streaming gratuito.");
        userRepository.save(user);
        auditRepository.save(new AuditLog(fullName, role.name(), "NOVO_CADASTRO", email,
                "Conta registrada com hash BCrypt (RF001/RNF006)."));
        return ResponseEntity.ok(user);
    }

    /**
     * Autentica as credenciais de um usuário cadastrado e verifica bloqueios administrativos (RF002, UC06).
     *
     * @param body mapa contendo {@code email} e {@code password}
     * @return resposta HTTP 200 com a conta autenticada ou HTTP 401/403 em caso de falha
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");

        Optional<User> opt = userRepository.findByEmailIgnoreCase(email);
        if (opt.isEmpty() || !platformService.verifyPassword(password, opt.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Credenciais inválidas. Verifique seu e-mail e senha (UC06)."));
        }
        User user = opt.get();
        if (user.isBlocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Esta conta encontra-se bloqueada pela administração (RF026)."));
        }
        if (request.getSession(false) != null) request.changeSessionId();
        request.getSession().setAttribute(SESSION_USER, user.getId());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(user);
    }

    @GetMapping("/auth/session")
    public ResponseEntity<User> currentSession(HttpServletRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(sessionUser(request));
    }

    @DeleteMapping("/auth/session")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.noContent().build();
    }

    private User sessionUser(HttpServletRequest request) {
        var session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SESSION_USER);
        if (userId instanceof Long id) {
            return userRepository.findById(id).filter(user -> !user.isBlocked())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping(value = "/auth/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(@RequestPart("picture") MultipartFile picture, HttpServletRequest request) {
        User user = sessionUser(request);
        try {
            var image = images.readUpload(picture);
            User updated = transactions.execute(status -> {
                User current = userRepository.findByIdForUpdate(user.getId())
                        .filter(account -> !account.isBlocked())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                String previous = current.getProfilePictureUrl();
                current.setProfilePictureUrl(images.save(image));
                images.remove(previous);
                return userRepository.save(current);
            });
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao ler a foto", e);
        }
    }

    @PutMapping("/auth/favorites/{trackId}")
    public Set<Long> setFavorite(@PathVariable Long trackId, @RequestBody Map<String, Boolean> body,
                                 HttpServletRequest request) {
        User user = sessionUser(request);
        return transactions.execute(status -> {
            User current = userRepository.findByIdForUpdate(user.getId())
                    .filter(account -> !account.isBlocked())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            if (Boolean.TRUE.equals(body.get("favorite"))) current.getFavoriteTrackIds().add(trackId);
            else current.getFavoriteTrackIds().remove(trackId);
            return Set.copyOf(current.getFavoriteTrackIds());
        });
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID id) {
        var image = images.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getMime()))
                .header("X-Content-Type-Options", "nosniff").body(image.getBytes());
    }

    /**
     * Expede token seguro de recuperação de acesso e redefine a credencial do usuário (RF003, UC06).
     *
     * @param body mapa contendo {@code email} e {@code newPassword}
     * @return confirmação da recuperação com o token gerado
     */
    @PostMapping("/auth/recover")
    public ResponseEntity<?> recoverPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = sessionUser(request);
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String newPassword = body.getOrDefault("newPassword", "");
        if (!user.getEmail().equalsIgnoreCase(email)
                || !platformService.verifyPassword(body.getOrDefault("currentPassword", ""), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Informe a senha atual da sua conta."));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "A nova senha deve ter pelo menos 6 caracteres."));
        }
        user.setRecoveryToken("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        user.setPasswordHash(platformService.hashPassword(newPassword));
        userRepository.save(user);
        auditRepository.save(new AuditLog(user.getFullName(), user.getRole().name(), "RECUPERACAO_SENHA", email,
                "Código expedido e credencial redefinida (RF003)."));
        return ResponseEntity.ok(Map.of(
                "message", "Procedimento de recuperação concluído.",
                "recoveryToken", user.getRecoveryToken()));
    }

    /**
     * Atualiza as informações cadastrais e biográficas do perfil pessoal do usuário (RF004).
     *
     * @param userId identificador do usuário
     * @param body campos a atualizar ({@code fullName}, {@code bio})
     * @return entidade {@link User} atualizada
     */
    @PutMapping("/auth/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId, @RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        User user = sessionUser(request);
        if (!user.getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (body.getOrDefault("fullName", "").length() > 255 || body.getOrDefault("bio", "").length() > 600) {
            return ResponseEntity.badRequest().body(Map.of("error", "Use até 255 caracteres no nome e 600 na biografia."));
        }
        if (body.containsKey("fullName") && !body.get("fullName").isBlank()) {
            user.setFullName(body.get("fullName").trim());
        }
        if (body.containsKey("bio")) {
            user.setBio(body.get("bio").trim());
        }
        userRepository.save(Objects.requireNonNull(user));
        return ResponseEntity.ok(user);
    }

    /**
     * Submete e publica uma nova obra sonora após validação técnica estrita (RF005, RF006, RF007, UC01).
     *
     * @param body metadados e parâmetros técnicos da faixa ou episódio
     * @return obra publicada ou erro HTTP 400 caso viole formato, bitrate ou resolução de capa
     */
    @PostMapping(value = "/catalog/tracks", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> publishTrack(@RequestBody Map<String, Object> body) {
        try {
            AudioTrack track = newTrack(body);
            savePublishedTrack(track);
            return ResponseEntity.ok(track);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/catalog/tracks/uploads")
    public List<AudioTrack> listUploadedTracks() {
        return trackRepository.findByAudioFileKeyIsNotNullAndStatusOrderByPublishedAtDesc(MediaStatus.ATIVO);
    }

    @PostMapping(value = "/catalog/tracks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTrack(@RequestPart("metadata") Map<String, Object> body,
                                         @RequestPart("audio") MultipartFile audio,
                                         @RequestPart(value = "cover", required = false) MultipartFile cover,
                                         HttpServletRequest request) {
        User owner = sessionUser(request);
        try {
            var coverImage = cover == null ? null : images.readUpload(cover);
            Map<String, Object> metadata = new HashMap<>(body);
            metadata.put("creatorUserId", owner.getId());
            if (coverImage != null) {
                metadata.put("coverWidth", coverImage.getWidth());
                metadata.put("coverHeight", coverImage.getHeight());
            }
            AudioTrack track = newTrack(metadata);
            track.setCoverUrl("grad-emerald");
            if (!(body.get("title") instanceof String title) || title.isBlank() || title.length() > 255) {
                throw new IllegalArgumentException("Informe um título com até 255 caracteres.");
            }
            if (track.getDurationSeconds() < 1 || track.getBitrateKbps() < 1) {
                throw new IllegalArgumentException("Informe uma duração e um bitrate válidos.");
            }
            track.setTitle(title.trim());
            String key = audioStorage.save(audio, track.getAudioFormat());
            track.setAudioFileKey(key);
            try {
                transactions.executeWithoutResult(status -> {
                    if (coverImage != null) track.setCoverUrl(images.save(coverImage));
                    savePublishedTrack(track);
                });
            } catch (RuntimeException e) {
                audioStorage.discard(key, e);
                throw e;
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(track);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao gravar áudio", e);
        }
    }

    @PatchMapping("/catalog/tracks/{trackId}")
    public Map<String, String> renameUploadedTrack(@PathVariable Long trackId, @RequestBody Map<String, String> body,
                                                   HttpServletRequest request) {
        User owner = sessionUser(request);
        String title = Objects.toString(body.get("title"), "").trim();
        if (title.isEmpty() || title.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um título com até 255 caracteres.");
        }
        if (trackRepository.renameUploadedTrack(trackId, owner.getId(), title, MediaStatus.ATIVO) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return Map.of("title", title);
    }

    private AudioTrack findUploadedTrack(Long trackId) {
        return trackRepository.findById(trackId)
                .filter(track -> track.getAudioFileKey() != null && track.getStatus() == MediaStatus.ATIVO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private AudioTrack newTrack(Map<String, Object> body) {
        String format = String.valueOf(body.getOrDefault("audioFormat", "WAV")).toUpperCase();
        int bitrate = Integer.parseInt(String.valueOf(body.getOrDefault("bitrateKbps", 1411)));
        int coverWidth = Integer.parseInt(String.valueOf(body.getOrDefault("coverWidth", 1400)));
        int coverHeight = Integer.parseInt(String.valueOf(body.getOrDefault("coverHeight", 1400)));

        platformService.validateTrackSubmission(format, bitrate, coverWidth, coverHeight);

        return new AudioTrack(
                String.valueOf(body.get("title")),
                MediaCategory.valueOf(String.valueOf(body.getOrDefault("category", "MUSICA"))),
                ReleaseModality.valueOf(String.valueOf(body.getOrDefault("modality", "SINGLE"))),
                String.valueOf(body.get("authors")),
                Long.valueOf(String.valueOf(body.getOrDefault("artistId", 1))),
                body.get("albumId") != null ? Long.valueOf(String.valueOf(body.get("albumId"))) : null,
                String.valueOf(body.getOrDefault("albumTitle", "Lançamento Independente")),
                String.valueOf(body.get("genre")),
                Integer.parseInt(String.valueOf(body.getOrDefault("releaseYear", 2026))),
                String.valueOf(body.getOrDefault("description", "")),
                String.valueOf(body.getOrDefault("tags", "independente")),
                format,
                bitrate,
                coverWidth,
                coverHeight,
                String.valueOf(body.getOrDefault("coverUrl", "grad-emerald")),
                Integer.parseInt(String.valueOf(body.getOrDefault("durationSeconds", 185))),
                1,
                0L, 0L, 0L, 0.0,
                Long.valueOf(String.valueOf(body.getOrDefault("creatorUserId", 2)))
        );
    }

    private void savePublishedTrack(AudioTrack track) {
        trackRepository.save(track);
        auditRepository.save(new AuditLog(track.getAuthors(), "CRIADOR", "PUBLICACAO_AUDIO", track.getTitle(),
                "Formato " + track.getAudioFormat() + " (" + track.getBitrateKbps() + " kbps), Capa "
                        + track.getCoverWidth() + "x" + track.getCoverHeight() + "px (UC01)."));
    }

    /**
     * Remove uma obra do catálogo ativo e encerra denúncias pendentes por perda de objeto (RF008, UC04).
     *
     * @param trackId identificador da obra a remover
     * @return obra atualizada com status {@link MediaStatus#REMOVIDO}
     */
    @DeleteMapping("/catalog/tracks/{trackId}")
    public ResponseEntity<?> deleteTrack(@PathVariable Long trackId, HttpServletRequest request) {
        User owner = sessionUser(request);
        AudioTrack track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(track.getCreatorUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        track.setStatus(MediaStatus.REMOVIDO);
        trackRepository.save(track);
        reportRepository.findAll().stream()
                .filter(r -> r.getTrackId().equals(trackId) && r.getStatus() == ReportStatus.PENDENTE)
                .forEach(r -> {
                    r.setStatus(ReportStatus.ENCERRADA_PERDA_OBJETO);
                    r.setTechnicalOpinion("Encerrada automaticamente por perda de objeto: obra excluída pelo autor (UC04).");
                    r.setResolvedAt(LocalDateTime.now());
                    reportRepository.save(r);
                });
        return ResponseEntity.ok(track);
    }

    /**
     * Gera URL temporária assinada com HMAC-SHA256 para reprodução segura da faixa (RNF005).
     *
     * @param trackId identificador da faixa
     * @param quality perfil de qualidade adaptativa solicitado (RF014)
     * @return mapa contendo a URL assinada e o token de sessão
     */
    @GetMapping("/stream/{trackId}/signed-url")
    public Map<String, String> getSignedStreamUrl(@PathVariable Long trackId,
                                                  @RequestParam(defaultValue = "high") String quality) {
        if ("original".equals(quality)) {
            findUploadedTrack(trackId);
            String token = platformService.generateSignedStreamToken(trackId, quality);
            return Map.of("streamUrl", "/api/stream/" + trackId + "/original?token=" + token,
                    "token", token, "quality", quality);
        }
        String token = platformService.generateSignedStreamToken(trackId, quality);
        return Map.of(
                "streamUrl", "/api/stream/" + trackId + "/audio.wav?quality=" + quality + "&token=" + token,
                "token", token,
                "quality", quality
        );
    }

    @GetMapping("/stream/{trackId}/original")
    public ResponseEntity<Resource> streamOriginal(@PathVariable Long trackId, @RequestParam String token) {
        if (!platformService.isValidStreamToken(trackId, "original", token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        AudioTrack track = findUploadedTrack(trackId);
        Resource audio = audioStorage.read(track.getAudioFileKey());
        if (!audio.isReadable()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        String contentType = switch (track.getAudioFormat()) {
            case "MP3" -> "audio/mpeg";
            case "FLAC" -> "audio/flac";
            default -> "audio/wav";
        };
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.noStore()).body(audio);
    }

    /**
     * Transmite o fluxo de áudio WAV com cabeçalhos de suporte a requisições parciais (RF012, RNF003).
     *
     * @param trackId identificador da faixa
     * @param quality perfil de qualidade ({@code high}, {@code medium}, {@code low})
     * @return fluxo binário WAV
     */
    @GetMapping(value = "/stream/{trackId}/audio.wav", produces = "audio/wav")
    public ResponseEntity<byte[]> streamAudio(@PathVariable Long trackId,
                                              @RequestParam(defaultValue = "high") String quality) {
        byte[] wavBytes = platformService.synthesizePlayableWav(trackId, quality);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.set("Accept-Ranges", "bytes");
        headers.setContentLength(wavBytes.length);
        return new ResponseEntity<>(wavBytes, headers, HttpStatus.OK);
    }

    /**
     * Registra uma audição no histórico e contabiliza estatísticas caso supere 30 segundos (RF015, RF022).
     *
     * @param body mapa com {@code trackId}, {@code userId} e {@code listenedSeconds}
     * @return resultado da validação estatística
     */
    @PostMapping("/telemetry/validate-play")
    public ResponseEntity<?> registerValidatedPlay(@RequestBody Map<String, Object> body) {
        Long trackId = Objects.requireNonNull(Long.valueOf(String.valueOf(body.get("trackId"))));
        Long userId = Long.valueOf(String.valueOf(body.getOrDefault("userId", 1)));
        int listenedSeconds = Integer.parseInt(String.valueOf(body.getOrDefault("listenedSeconds", 31)));

        AudioTrack track = trackRepository.findById(trackId).orElseThrow();
        boolean valid = listenedSeconds >= 30;
        if (valid) {
            track.setPlaysTotal(track.getPlaysTotal() + 1);
            track.setPlaysLast24h(track.getPlaysLast24h() + 1);
            track.setUniqueListeners(track.getUniqueListeners() + 1);
            trackRepository.save(track);
        }
        PlayHistory history = new PlayHistory(userId, trackId, track.getTitle(), track.getAuthors(), listenedSeconds, valid);
        historyRepository.save(history);
        return ResponseEntity.ok(Map.of("validatedOver30s", valid, "playsTotal", track.getPlaysTotal()));
    }

    /**
     * Executa pesquisa parametrizada sobre o acervo em tempo inferior a 400ms (RF016, RNF004, UC05).
     *
     * @param q termo livre de pesquisa
     * @param category filtro de categoria
     * @param genre filtro de gênero
     * @param tag filtro de etiqueta
     * @return resultados agrupados e tempo de execução em milissegundos
     */
    @GetMapping("/catalog/search")
    public Map<String, Object> search(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String genre,
                                      @RequestParam(required = false) String tag) {
        return platformService.searchCatalog(q, category, genre, tag);
    }

    /**
     * Registra uma denúncia de violação autoral ou conduta imprópria na fila do Moderador (RF024, UC04).
     *
     * @param body dados da denúncia submetida pelo ouvinte
     * @return entidade {@link AbuseReport} criada
     */
    @PostMapping("/moderation/reports")
    public ResponseEntity<?> createReport(@RequestBody Map<String, Object> body) {
        Long trackId = Objects.requireNonNull(Long.valueOf(String.valueOf(body.get("trackId"))));
        AudioTrack track = trackRepository.findById(trackId).orElseThrow();
        AbuseReport report = new AbuseReport(
                trackId,
                track.getTitle(),
                track.getAuthors(),
                Long.valueOf(String.valueOf(body.getOrDefault("reporterUserId", 1))),
                String.valueOf(body.getOrDefault("reporterName", "Ouvinte Autenticado")),
                String.valueOf(body.get("reason")),
                String.valueOf(body.getOrDefault("description", ""))
        );
        reportRepository.save(report);
        return ResponseEntity.ok(report);
    }

    /**
     * Aplica a deliberação do Moderador sobre uma denúncia pendente (RF025, UC04).
     *
     * @param reportId identificador do relatório de denúncia
     * @param body decisão ({@code SUSPENDER} ou {@code INDEFERIR}) e parecer técnico
     * @return relatório atualizado e registrado no livro de auditoria
     */
    @PostMapping("/moderation/reports/{reportId}/decide")
    public ResponseEntity<?> moderateReport(@PathVariable Long reportId, @RequestBody Map<String, String> body) {
        AbuseReport report = reportRepository.findById(Objects.requireNonNull(reportId)).orElseThrow();
        String decision = body.getOrDefault("decision", "SUSPENDER");
        String opinion = body.getOrDefault("technicalOpinion", "Parecer técnico registrado.");
        String moderatorName = body.getOrDefault("moderatorName", "Moderador");

        Long reportedTrackId = Objects.requireNonNull(report.getTrackId());
        AudioTrack track = trackRepository.findById(reportedTrackId).orElse(null);
        if (track == null || track.getStatus() == MediaStatus.REMOVIDO) {
            report.setStatus(ReportStatus.ENCERRADA_PERDA_OBJETO);
            report.setTechnicalOpinion("Encerrada por perda de objeto: obra já havia sido excluída pelo autor (UC04).");
        } else if ("SUSPENDER".equalsIgnoreCase(decision)) {
            track.setStatus(MediaStatus.SUSPENSO);
            trackRepository.save(track);
            report.setStatus(ReportStatus.PROCEDENTE_SUSPENSO);
            report.setTechnicalOpinion(opinion);
        } else {
            report.setStatus(ReportStatus.INDEFERIDA_ARQUIVADA);
            report.setTechnicalOpinion(opinion);
        }
        report.setModeratorName(moderatorName);
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);

        auditRepository.save(new AuditLog(moderatorName, "MODERADOR", "DELIBERACAO_DENUNCIA_" + report.getStatus(),
                report.getTrackTitle(), opinion));
        return ResponseEntity.ok(report);
    }

    /**
     * Altera papéis de permissão RBAC ou aplica bloqueio de conta, protegendo o último Administrador Mestre (RF026, UC09).
     *
     * @param userId identificador do usuário alvo
     * @param body novo papel ({@code role}) e/ou estado de bloqueio ({@code blocked})
     * @return conta atualizada ou erro HTTP 400 caso tente revogar o único Administrador Mestre ativo
     */
    @PutMapping("/admin/users/{userId}")
    public ResponseEntity<?> adminUpdateUser(@PathVariable Long userId, @RequestBody Map<String, Object> body,
                                             HttpServletRequest request) {
        if (sessionUser(request).getRole() != Role.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        User target = userRepository.findById(Objects.requireNonNull(userId)).orElseThrow();
        Role newRole = body.containsKey("role") ? Role.valueOf(String.valueOf(body.get("role"))) : target.getRole();
        boolean newBlocked = body.containsKey("blocked")
                ? Boolean.parseBoolean(String.valueOf(body.get("blocked"))) : target.isBlocked();

        if (target.getRole() == Role.ADMINISTRADOR && (!newRole.equals(Role.ADMINISTRADOR) || newBlocked)) {
            long activeAdmins = userRepository.countByRoleAndBlockedFalse(Role.ADMINISTRADOR);
            if (activeAdmins <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "Ação barrada (UC09): Não é permitido revogar privilégios ou bloquear o único Administrador Mestre ativo."));
            }
        }

        target.setRole(newRole);
        target.setBlocked(newBlocked);
        userRepository.save(target);
        auditRepository.save(new AuditLog("Administrador Mestre", "ADMINISTRADOR", "ALTERACAO_CONTA",
                target.getEmail(), "Novo papel: " + newRole + " | Bloqueado: " + newBlocked));
        return ResponseEntity.ok(target);
    }
}
