package br.com.socialconnect.api.doacoes.controller;

import br.com.socialconnect.api.doacoes.dto.DoacaoRequestDTO;
import br.com.socialconnect.api.doacoes.dto.DoacaoResponseDTO;
import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import br.com.socialconnect.api.doadores.model.Doador;
import br.com.socialconnect.api.doadores.model.TipoDoador;
import br.com.socialconnect.api.doadores.repository.DoadorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class DoacaoControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @LocalServerPort
    private int port;

    @Autowired
    private DoadorRepository doadorRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Long idDoadorCriado;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/doacoes";
    }

    @BeforeEach
    void setUp() {
        if (idDoadorCriado == null && doadorRepository != null) {
            Doador doador = Doador.builder()
                    .nome("Doador Integração")
                    .tipo(TipoDoador.PESSOA_FISICA)
                    .email("doador@teste.com")
                    .telefone("11988887777")
                    .build();
            Doador salvo = doadorRepository.save(doador);
            idDoadorCriado = salvo.getIdDoador();
        }
    }

    @Test
    @DisplayName("Deve criar doação quando dados válidos")
    void deveCriarDoacaoQuandoDadosValidos() throws Exception {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        DoacaoRequestDTO dto = new DoacaoRequestDTO(
                idDoadorCriado,
                LocalDate.now(),
                new BigDecimal("150.00"),
                TipoDoacao.ALIMENTO,
                "Cesta de legumes"
        );
        String requestJson = objectMapper.writeValueAsString(dto);

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertEquals(201, response.statusCode());
        DoacaoResponseDTO body = objectMapper.readValue(response.body(), DoacaoResponseDTO.class);
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.idDoacao(), "ID da doação não deve ser nulo");
        Assertions.assertEquals(new BigDecimal("150.00"), body.valor());
    }

    @Test
    @DisplayName("Deve retornar 400 quando data futura")
    void deveRetornar400QuandoDataFutura() throws Exception {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        DoacaoRequestDTO dto = new DoacaoRequestDTO(
                idDoadorCriado,
                LocalDate.now().plusDays(10),
                new BigDecimal("50.00"),
                TipoDoacao.ROUPA,
                "Agasalhos"
        );
        String requestJson = objectMapper.writeValueAsString(dto);

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(
                response.body() != null && response.body().toLowerCase().contains("futuro"),
                "A resposta deve conter mensagem sobre data no futuro"
        );
    }
}
