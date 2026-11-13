package br.ufscar.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suíte de Testes de Sistema (End-to-End Integration Tests) utilizando {@link MockMvc}.
 * Verifica a camada MVC Thymeleaf, internacionalização (PT/EN) e os casos de uso UC01 a UC09.
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class StreamingSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Arquitetura MVC (Thymeleaf + i18n): Deve renderizar a view principal em Português e Inglês")
    void shouldRenderThymeleafMvcViewInPortugueseAndEnglish() throws Exception {
        mockMvc.perform(get("/").param("lang", "pt"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("activeLang", "pt"))
                .andExpect(model().attributeExists("tracks", "artists", "albums", "playlists"));

        mockMvc.perform(get("/").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("activeLang", "en"));
    }

    @Test
    @DisplayName("UC06 (RF001, RF002, RF003): Deve cadastrar usuário, bloquear e-mail duplicado, autenticar e recuperar senha")
    void shouldExecuteCompleteAuthenticationAndRegistrationLifecycle() throws Exception {
        String uniqueEmail = "novo.estudante." + System.currentTimeMillis() + "@ufscar.br";
        Map<String, String> regPayload = Map.of(
                "fullName", "Estudante Teste UFSCar",
                "email", uniqueEmail,
                "password", "senha123",
                "role", "OUVINTE"
        );

        // 1. Novo cadastro com sucesso (RF001)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(uniqueEmail));

        // 2. Bloqueio de e-mail duplicado (Exceção UC06)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regPayload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("UC06")));

        // 3. Login válido (RF002)
        var login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", uniqueEmail, "password", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Estudante Teste UFSCar"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(get("/api/auth/session").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(uniqueEmail));

        // 4. Recuperação de acesso (RF003)
        mockMvc.perform(post("/api/auth/recover")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", uniqueEmail,
                                "currentPassword", "senha123", "newPassword", "novaSenha456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recoveryToken", startsWith("REC-")));
    }

    @Test
    @DisplayName("UC01 (RF005 e RF006): Deve publicar áudio válido e barrar submissões fora do padrão técnico")
    void shouldValidateAndPublishAudioTrack() throws Exception {
        Map<String, Object> validTrack = Map.ofEntries(
                Map.entry("title", "Sinfonia Universitária"),
                Map.entry("category", "MUSICA"),
                Map.entry("modality", "SINGLE"),
                Map.entry("authors", "Orquestra UFSCar"),
                Map.entry("genre", "Clássica"),
                Map.entry("releaseYear", 2026),
                Map.entry("tags", "orquestra, wav, lossless"),
                Map.entry("audioFormat", "WAV"),
                Map.entry("bitrateKbps", 1411),
                Map.entry("coverWidth", 1600),
                Map.entry("coverHeight", 1600)
        );

        mockMvc.perform(post("/api/catalog/tracks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTrack)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sinfonia Universitária"));

        Map<String, Object> invalidTrack = Map.of(
                "title", "Faixa Inválida",
                "authors", "Autor",
                "genre", "Pop",
                "audioFormat", "MP3",
                "bitrateKbps", 128,
                "coverWidth", 800,
                "coverHeight", 800
        );

        mockMvc.perform(post("/api/catalog/tracks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTrack)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("320 kbps")));
    }

    @Test
    @DisplayName("UC02 (RF012, RF015, RNF005): Deve transmitir áudio WAV assinado e contabilizar audição > 30s")
    void shouldStreamAudioAndRecordValidatedAuditionOver30Seconds() throws Exception {
        mockMvc.perform(get("/api/stream/1/signed-url").param("quality", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streamUrl", containsString("/api/stream/1/audio.wav")));

        mockMvc.perform(get("/api/stream/1/audio.wav").param("quality", "high"))
                .andExpect(status().isOk())
                .andExpect(header().string("Accept-Ranges", "bytes"))
                .andExpect(content().contentType("audio/wav"));

        mockMvc.perform(post("/api/telemetry/validate-play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("trackId", 1, "userId", 1, "listenedSeconds", 35))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validatedOver30s").value(true));
    }

    @Test
    @DisplayName("UC09 (RF026): Deve impedir a revogação ou bloqueio do único Administrador Mestre ativo")
    void shouldPreventRevokingSoleActiveMasterAdministrator() throws Exception {
        var login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "admin@ufscar.br", "password", "123456"))))
                .andExpect(status().isOk())
                .andReturn();
        // O usuário ID 4 é o único Administrador Mestre inicializado por DataInitializer
        mockMvc.perform(put("/api/admin/users/4")
                        .session((MockHttpSession) login.getRequest().getSession(false))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "OUVINTE", "blocked", true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("UC09")));
    }
}
