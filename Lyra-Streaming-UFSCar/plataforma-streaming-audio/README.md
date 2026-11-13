# Lyra UFSCar — Plataforma Integrada de Streaming e Compartilhamento de Áudio Gratuito

**Universidade Federal de São Carlos (UFSCar)**  
**Centro de Ciências Exatas e de Tecnologia — Departamento de Engenharia Elétrica**  
**Disciplina:** Tópicos em Engenharia de Software  
**Docente Responsável:** Prof. Dr. Delano Medeiros Beder  
**Integrantes do Grupo 03:**
- João Pedro Elias (RA: 824565)
- Patrick Ladeira Kachan (RA: 769211)
- Tiago Felix Carrare (RA: 800149)

---

## 1. Padrão Arquitetural MVC (Model-View-Controller) e Persistência JPA

O projeto está organizado rigorosamente em camadas seguindo o padrão **MVC com Spring Boot 3.2.5**:

1. **Camada de Visão (`src/main/resources/templates/index.html` — Thymeleaf + i18n + Temas):**
   - Renderizada pelo motor **Thymeleaf** com suporte nativo a internacionalização (`messages_pt.properties` e `messages_en.properties`) e alternância instantânea entre **Português (`PT-BR`)** e **Inglês (`EN-US`)**, além de **Tema Claro (Light)** e **Tema Escuro (Dark)**.
   - Preserva a instância do reprodutor de áudio ativa durante toda a navegação (`RNF001`).
2. **Camada de Controladores (`br.ufscar.streaming.controller`):**
   - `WebMvcController.java` (`@Controller`): processa as rotas de página, resolve o idioma ativo e popula o objeto `Model` do Thymeleaf.
   - `ApiController.java` (`@RestController`): processa operações assíncronas para os casos de uso `UC01` a `UC09`.
3. **Camada de Domínio e Serviço (`br.ufscar.streaming.domain` e `br.ufscar.streaming.service`):**
   - Entidades JPA documentadas via Javadoc: `User`, `ArtistProfile`, `Album`, `AudioTrack`, `Playlist`, `PlayHistory`, `AbuseReport`, `AuditLog` e enumerações de domínio.
   - `PlatformService.java`: implementa hash BCrypt (`RNF006`), URLs assinadas HMAC-SHA256 (`RNF005`), validação de upload lossless/capa `1400×1400` (`RF005`, `RF006`), síntese de streaming WAV (`RF012`, `RF014`), busca indexada `<400ms` (`RNF004`) e backup diário (`RNF009`).
4. **Camada de Persistência (`br.ufscar.streaming.repository` — Spring Data JPA):**
   - Repositórios tipados integrados ao banco relacional H2 embarcado (`./data/streamingdb`).

---

## 2. Como Executar a Aplicação e os Testes no VS Code

### Executar a Plataforma Web
1. Abra a pasta `plataforma-streaming-audio` no **VS Code**.
2. Pressione **`F5`** (ou clique em *Run > Start Debugging* na configuração **"Executar Plataforma Streaming MVC (Spring Boot)"**).
3. Acesse `http://localhost:8080` (ou `http://localhost:8080/?lang=en` para iniciar diretamente em inglês).

### Executar os Testes Unitários e de Sistema
- **Pela interface do VS Code:** Abra o ícone de tubo de ensaio (**Testing**) na barra lateral esquerda do VS Code e clique em **Run Tests** para executar:
  - `PlatformServiceUnitTest.java` (7 Testes Unitários JUnit 5 + Mockito)
  - `StreamingSystemIntegrationTest.java` (5 Testes de Sistema Ponta a Ponta com `@SpringBootTest` e `MockMvc`)
- **Via terminal Maven:**
  ```bash
  mvn test
  ```

### Publicar um arquivo de áudio

1. Entre na sua conta. Abra **Publicar áudio** e selecione um arquivo WAV, FLAC ou MP3 de até 150 MB.
2. Confira os metadados e, se quiser, selecione uma capa JPEG ou PNG de até 8 MB. A imagem deve ter pelo menos 1400 × 1400 px. O formulário exige bitrate declarado de pelo menos 320 kbps para MP3.
3. Clique em **Publicar**. A faixa fica no catálogo e você pode renomeá-la ou removê-la em **Minhas publicações**.

O arquivo original fica em `./data/audio`. Os metadados e a capa ficam no banco H2. Faixas sem imagem usam o gradiente do catálogo.

### Conta e foto de perfil

Na página inicial, entre com e-mail e senha ou clique em **Criar conta**. Depois de entrar, abra **Minha conta** e selecione uma foto JPEG ou PNG de até 8 MB em **Perfil**. O nome, a biografia, a foto e os favoritos são gravados no banco. A sessão é mantida ao recarregar enquanto o servidor estiver ativo.
