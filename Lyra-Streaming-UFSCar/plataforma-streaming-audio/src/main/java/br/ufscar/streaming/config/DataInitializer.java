package br.ufscar.streaming.config;

import br.ufscar.streaming.domain.*;
import br.ufscar.streaming.repository.*;
import br.ufscar.streaming.service.PlatformService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Componente de inicialização que popula o banco de dados H2 com os 4 perfis RBAC,
 * artistas, álbuns, faixas musicais, episódios de podcast, listas de reprodução e denúncias.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ArtistProfileRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final AudioTrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final PlayHistoryRepository historyRepository;
    private final AbuseReportRepository reportRepository;
    private final AuditLogRepository auditRepository;
    private final PlatformService platformService;

    public DataInitializer(UserRepository userRepository,
                           ArtistProfileRepository artistRepository,
                           AlbumRepository albumRepository,
                           AudioTrackRepository trackRepository,
                           PlaylistRepository playlistRepository,
                           PlayHistoryRepository historyRepository,
                           AbuseReportRepository reportRepository,
                           AuditLogRepository auditRepository,
                           PlatformService platformService) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
        this.historyRepository = historyRepository;
        this.reportRepository = reportRepository;
        this.auditRepository = auditRepository;
        this.platformService = platformService;
    }

    @Override
    @SuppressWarnings("null")
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        String defaultHash = platformService.hashPassword("123456");

        User ouvinte = new User("João Pedro Elias", "joao.ouvinte@ufscar.br", defaultHash, Role.OUVINTE,
                "Estudante de Engenharia na UFSCar e entusiasta de música independente e podcasts acadêmicos.");
        ouvinte.setFavoriteTrackIds(Set.of(1L, 3L));
        ouvinte.setFollowedArtistIds(Set.of(1L, 2L));

        User criador = new User("Patrick Ladeira Kachan", "patrick.criador@ufscar.br", defaultHash, Role.CRIADOR,
                "Produtor musical independente e criador do podcast Engenharia em Frequência.");
        criador.setFavoriteTrackIds(Set.of(2L));
        criador.setFollowedArtistIds(Set.of(1L));

        User moderador = new User("Tiago Felix Carrare", "tiago.moderador@ufscar.br", defaultHash, Role.MODERADOR,
                "Responsável pela curadoria normativa, direitos autorais e integridade do catálogo.");

        User admin = new User("Prof. Delano Medeiros Beder", "admin@ufscar.br", defaultHash, Role.ADMINISTRADOR,
                "Administrador Mestre de Governança e Auditoria da Plataforma.");

        userRepository.saveAll(Objects.requireNonNull(List.of(ouvinte, criador, moderador, admin)));

        ArtistProfile a1 = new ArtistProfile("Horizonte Acústico",
                "Projeto autoral de música instrumental brasileira e synth-jazz gravado em formatos lossless (FLAC/WAV).",
                "grad-emerald", "MPB, Instrumental, Jazz", criador.getId(), 1420);
        ArtistProfile a2 = new ArtistProfile("Podcast Engenharia em Frequência",
                "Debates semanais sobre arquitetura de software, sistemas distribuídos e inovação aberta na UFSCar.",
                "grad-indigo", "Podcast, Tecnologia, Educação", criador.getId(), 980);
        ArtistProfile a3 = new ArtistProfile("Aurora Boreal Sintética",
                "Duo de música eletrônica atmosférica e ambient lo-fi para foco e programação.",
                "grad-amber", "Synthwave, Lo-Fi, Eletrônica", criador.getId(), 2150);
        artistRepository.saveAll(Objects.requireNonNull(List.of(a1, a2, a3)));

        Album alb1 = new Album("Crônicas do Cerrado (Deluxe Lossless)", ReleaseModality.ALBUM,
                a1.getId(), a1.getArtisticName(), 2026, "2026-08-15",
                "Álbum autoral masterizado em 24-bit WAV sem compressão.", "grad-emerald", "Horizonte Acústico, Quarteto São Carlos");
        Album alb2 = new Album("Temporada 2026: Arquitetura & Escala", ReleaseModality.PODCAST,
                a2.getId(), a2.getArtisticName(), 2026, "2026-09-01",
                "Série de episódios técnicos sobre engenharia de software moderna.", "grad-indigo", "Patrick Kachan, João Elias, Tiago Carrare");
        Album alb3 = new Album("Noites no Campus EP", ReleaseModality.EP,
                a3.getId(), a3.getArtisticName(), 2026, "2026-09-18",
                "EP com 3 composições eletrônicas para sessões noturnas de estudo.", "grad-amber", "Aurora Boreal Sintética");
        albumRepository.saveAll(Objects.requireNonNull(List.of(alb1, alb2, alb3)));

        AudioTrack t1 = new AudioTrack("Vento entre os Ipês", MediaCategory.MUSICA, ReleaseModality.ALBUM,
                "Horizonte Acústico", a1.getId(), alb1.getId(), alb1.getTitle(), "MPB Instrumental", 2026,
                "Faixa de abertura gravada em WAV linear sem perdas.", "mpb, instrumental, acustico, wav",
                "WAV", 1411, 1600, 1600, "grad-emerald", 214, 1, 4820, 640, 3190, 88.5, criador.getId());

        AudioTrack t2 = new AudioTrack("Alvorada na Praça XV", MediaCategory.MUSICA, ReleaseModality.ALBUM,
                "Horizonte Acústico feat. Quarteto São Carlos", a1.getId(), alb1.getId(), alb1.getTitle(), "Jazz Brasileiro", 2026,
                "Arranjo para violão de 7 cordas e sopros em alta definição FLAC.", "jazz, flac, brasil, lossless",
                "FLAC", 1050, 1400, 1400, "grad-emerald", 198, 2, 3590, 490, 2410, 84.0, criador.getId());

        AudioTrack t3 = new AudioTrack("Ep. #12 – Arquitetura SPA e Streaming Adaptativo HLS", MediaCategory.PODCAST, ReleaseModality.PODCAST,
                "Podcast Engenharia em Frequência", a2.getId(), alb2.getId(), alb2.getTitle(), "Tecnologia & Software", 2026,
                "Análise prática sobre baixa latência (<2s), RBAC e entrega contínua de áudio na web.", "podcast, engenharia, software, ufscar, spa",
                "MP3", 320, 1400, 1400, "grad-indigo", 1840, 1, 2910, 520, 2180, 79.2, criador.getId());

        AudioTrack t4 = new AudioTrack("Compilando Estrelas (Lo-Fi Study Mix)", MediaCategory.MUSICA, ReleaseModality.EP,
                "Aurora Boreal Sintética", a3.getId(), alb3.getId(), alb3.getTitle(), "Synthwave / Lo-Fi", 2026,
                "Texturas analógicas e batidas suaves para concentração profunda.", "lofi, synthwave, estudo, foco",
                "FLAC", 980, 2000, 2000, "grad-amber", 242, 1, 6120, 890, 4450, 91.4, criador.getId());

        AudioTrack t5 = new AudioTrack("Pulso Binário (Single Edit)", MediaCategory.MUSICA, ReleaseModality.SINGLE,
                "Aurora Boreal Sintética", a3.getId(), alb3.getId(), "Pulso Binário - Single", "Eletrônica", 2026,
                "Single experimental em MP3 320 kbps.", "eletronica, single, sintetizador",
                "MP3", 320, 1400, 1400, "grad-rose", 176, 1, 1890, 310, 1420, 76.8, criador.getId());

        trackRepository.saveAll(Objects.requireNonNull(List.of(t1, t2, t3, t4, t5)));

        Playlist p1 = new Playlist("Foco & Programação na UFSCar",
                "Seleção oficial de faixas instrumentais e lo-fi sem interrupções para maratonas de código.",
                true, ouvinte.getId(), ouvinte.getFullName(), List.of(t4.getId(), t1.getId(), t2.getId()));
        Playlist p2 = new Playlist("Maratona Engenharia de Software",
                "Episódios fundamentais e trilhas de estudo.",
                true, criador.getId(), criador.getFullName(), List.of(t3.getId(), t5.getId()));
        playlistRepository.saveAll(Objects.requireNonNull(List.of(p1, p2)));

        historyRepository.save(new PlayHistory(ouvinte.getId(), t4.getId(), t4.getTitle(), t4.getAuthors(), 180, true));
        historyRepository.save(new PlayHistory(ouvinte.getId(), t1.getId(), t1.getTitle(), t1.getAuthors(), 145, true));

        AbuseReport rep1 = new AbuseReport(t5.getId(), t5.getTitle(), t5.getAuthors(), ouvinte.getId(),
                ouvinte.getFullName(), "Suspeita de sample não licenciado",
                "O trecho entre 01:15 e 01:30 apresenta semelhança com fonograma protegido sem atribuição explícita na ficha técnica.");
        reportRepository.save(rep1);

        auditRepository.save(new AuditLog("SISTEMA", "ADMINISTRADOR", "INICIALIZACAO_BASE",
                "Catálogo Inicial", "Base H2 inicializada com 4 atores RBAC, 5 obras sonoras e rotina de backup ativa."));
    }
}
