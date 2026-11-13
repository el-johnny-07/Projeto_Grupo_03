package br.ufscar.streaming.controller;

import br.ufscar.streaming.domain.MediaStatus;
import br.ufscar.streaming.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

/**
 * Controlador MVC responsável por processar requisições de páginas web e popular o {@link Model}
 * renderizado pelo motor de templates Thymeleaf.
 * Mantém compatibilidade com a reprodução contínua de áudio (RNF001) e suporte bilíngue (PT/EN).
 */
@Controller
public class WebMvcController {

    private final UserRepository userRepository;
    private final ArtistProfileRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final AudioTrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final AbuseReportRepository reportRepository;
    private final AuditLogRepository auditRepository;

    public WebMvcController(UserRepository userRepository,
                            ArtistProfileRepository artistRepository,
                            AlbumRepository albumRepository,
                            AudioTrackRepository trackRepository,
                            PlaylistRepository playlistRepository,
                            AbuseReportRepository reportRepository,
                            AuditLogRepository auditRepository) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
        this.reportRepository = reportRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Renderiza a visão principal MVC com os dados consolidados do catálogo, artistas, playlists
     * e estado inicial de idioma e tema visual.
     *
     * @param section aba ou módulo inicial a exibir
     * @param lang código opcional de idioma ({@code pt} ou {@code en})
     * @param locale localidade resolvida pelo Spring MVC
     * @param model modelo de dados injetado no template Thymeleaf {@code index.html}
     * @return nome lógico da view Thymeleaf ({@code index})
     */
    @GetMapping({"/", "/explorar", "/busca", "/artistas", "/biblioteca", "/estudio", "/telemetria", "/moderacao", "/admin"})
    public String renderMainView(@RequestParam(name = "section", defaultValue = "explorar") String section,
                                 @RequestParam(name = "lang", required = false) String lang,
                                 Locale locale,
                                 Model model) {
        String activeLang = (lang != null && lang.toLowerCase().startsWith("en"))
                || (locale != null && locale.getLanguage().startsWith("en")) ? "en" : "pt";

        model.addAttribute("initialSection", section);
        model.addAttribute("activeLang", activeLang);
        model.addAttribute("tracks", trackRepository.findByStatusOrderByPlaysLast24hDesc(MediaStatus.ATIVO));
        model.addAttribute("artists", artistRepository.findAll());
        model.addAttribute("albums", albumRepository.findAll());
        model.addAttribute("playlists", playlistRepository.findAll());
        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("pendingReportsCount",
                reportRepository.findAll().stream().filter(r -> r.getStatus().name().equals("PENDENTE")).count());
        model.addAttribute("auditLogsCount", auditRepository.count());
        return "index";
    }
}
