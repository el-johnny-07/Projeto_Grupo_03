package br.ufscar.streaming.service;

import br.ufscar.streaming.domain.AudioTrack;
import br.ufscar.streaming.domain.MediaCategory;
import br.ufscar.streaming.domain.ReleaseModality;
import br.ufscar.streaming.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Suíte de Testes Unitários para validação isolada das regras de negócio em {@link PlatformService}.
 * Cobre os requisitos RF005, RF006, RF012, RF014, RF016, RNF004, RNF005 e RNF006.
 */
@ExtendWith(MockitoExtension.class)
class PlatformServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private ArtistProfileRepository artistRepository;
    @Mock private AlbumRepository albumRepository;
    @Mock private AudioTrackRepository trackRepository;
    @Mock private PlaylistRepository playlistRepository;
    @Mock private PlayHistoryRepository historyRepository;
    @Mock private AbuseReportRepository reportRepository;
    @Mock private AuditLogRepository auditRepository;

    private PlatformService platformService;

    @BeforeEach
    void setUp() {
        platformService = new PlatformService(
                userRepository, artistRepository, albumRepository, trackRepository,
                playlistRepository, historyRepository, reportRepository, auditRepository
        );
    }

    @Test
    @DisplayName("RNF006: Deve aplicar hash BCrypt com salt e validar credenciais corretamente")
    void shouldHashAndVerifyPasswordWithBCrypt() {
        String raw = "SenhaSeguraUFSCar2026!";
        String hash = platformService.hashPassword(raw);

        assertNotNull(hash);
        assertNotEquals(raw, hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(platformService.verifyPassword(raw, hash));
        assertFalse(platformService.verifyPassword("senhaIncorreta", hash));
    }

    @Test
    @DisplayName("RF005 e RF006 (UC01): Deve aprovar formatos lossless (WAV/FLAC) e MP3 >= 320 kbps com capa >= 1400x1400")
    void shouldAcceptValidAudioFormatsAndCoverResolution() {
        assertDoesNotThrow(() -> platformService.validateTrackSubmission("WAV", 1411, 1400, 1400));
        assertDoesNotThrow(() -> platformService.validateTrackSubmission("FLAC", 1050, 1600, 1600));
        assertDoesNotThrow(() -> platformService.validateTrackSubmission("MP3", 320, 2000, 2000));
    }

    @Test
    @DisplayName("RF005 (Exceção UC01): Deve rejeitar formato não suportado ou MP3 com bitrate inferior a 320 kbps")
    void shouldRejectInvalidAudioFormatOrLowBitrateMp3() {
        IllegalArgumentException exFormat = assertThrows(IllegalArgumentException.class,
                () -> platformService.validateTrackSubmission("OGG", 500, 1400, 1400));
        assertTrue(exFormat.getMessage().contains("RF005"));

        IllegalArgumentException exBitrate = assertThrows(IllegalArgumentException.class,
                () -> platformService.validateTrackSubmission("MP3", 128, 1400, 1400));
        assertTrue(exBitrate.getMessage().contains("320 kbps"));
    }

    @Test
    @DisplayName("RF006 (Exceção UC01): Deve rejeitar imagem de capa com resolução inferior a 1400x1400 pixels")
    void shouldRejectCoverResolutionBelow1400x1400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> platformService.validateTrackSubmission("WAV", 1411, 1200, 1400));
        assertTrue(ex.getMessage().contains("1400x1400"));
    }

    @Test
    @DisplayName("RNF005: Deve gerar token de streaming temporário assinado via HMAC-SHA256")
    void shouldGenerateSignedStreamToken() {
        String token = platformService.generateSignedStreamToken(1L, "high");
        assertNotNull(token);
        assertTrue(token.contains("."));
        String[] parts = token.split("\\.");
        assertEquals(2, parts.length);
        assertTrue(Long.parseLong(parts[0]) > 0);
    }

    @Test
    @DisplayName("RF012 e RF014: Deve sintetizar fluxo WAV válido com cabeçalho RIFF/WAVE e adaptação de taxa de amostragem")
    void shouldSynthesizeValidPlayableWavForDifferentQualities() {
        byte[] highWav = platformService.synthesizePlayableWav(1L, "high");
        byte[] lowWav = platformService.synthesizePlayableWav(1L, "low");

        assertEquals("RIFF", new String(highWav, 0, 4, StandardCharsets.US_ASCII));
        assertEquals("WAVE", new String(highWav, 8, 4, StandardCharsets.US_ASCII));
        assertTrue(highWav.length > lowWav.length, "Fluxo em alta qualidade deve possuir maior densidade de amostras que baixa qualidade");
    }

    @Test
    @DisplayName("RF016 e RNF004 (UC05): Deve filtrar catálogo em tempo inferior a 400ms e sugerir termos quando vazio")
    void shouldSearchCatalogUnder400msAndProvideFallbackSuggestions() {
        AudioTrack sample = new AudioTrack(
                "Vento entre os Ipês", MediaCategory.MUSICA, ReleaseModality.ALBUM,
                "Horizonte Acústico", 1L, 1L, "Crônicas do Cerrado", "MPB Instrumental", 2026,
                "Faixa WAV", "mpb, instrumental, wav", "WAV", 1411, 1600, 1600,
                "grad-emerald", 214, 1, 100L, 20L, 80L, 85.0, 2L
        );
        when(trackRepository.findAll()).thenReturn(List.of(sample));
        when(trackRepository.findByStatusOrderByPlaysLast24hDesc(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(sample));

        Map<String, Object> hitResult = platformService.searchCatalog("Vento", "MUSICA", "mpb", "wav");
        List<?> found = (List<?>) hitResult.get("tracks");
        double elapsedMs = ((Number) hitResult.get("elapsedMs")).doubleValue();

        assertEquals(1, found.size());
        assertTrue(elapsedMs < 400.0, "Tempo de busca deve ser inferior a 400ms (RNF004)");

        Map<String, Object> emptyResult = platformService.searchCatalog("TermoInexistenteXYZ", "TODOS", "todos", "");
        List<?> emptyTracks = (List<?>) emptyResult.get("tracks");
        assertTrue(emptyTracks.isEmpty());
        assertNotNull(emptyResult.get("suggestedTerms"));
    }
}
