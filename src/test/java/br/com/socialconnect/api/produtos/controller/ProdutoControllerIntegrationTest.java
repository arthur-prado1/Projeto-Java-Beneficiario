package br.com.socialconnect.api.produtos.controller;

import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ProdutoControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @LocalServerPort
    private int port;

    @Autowired
    private ProdutoRepository produtoRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/produtos";
    }

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar produto quando dados válidos (espera 201)")
    void deveCriarProdutoQuandoDadosValidos() throws Exception {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Arroz Tipo 1 5kg",
                CategoriaProduto.ALIMENTO,
                20,
                5,
                "pacote"
        );
        String jsonPayload = objectMapper.writeValueAsString(dto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertTrue(response.headers().firstValue("Location").isPresent());

        JsonNode responseBody = objectMapper.readTree(response.body());
        Assertions.assertNotNull(responseBody.get("idProduto"));
        Assertions.assertEquals("Arroz Tipo 1 5kg", responseBody.get("nome").asText());
        Assertions.assertEquals("ALIMENTO", responseBody.get("categoria").asText());
        Assertions.assertFalse(responseBody.get("estoqueBaixo").asBoolean());
    }

    @Test
    @DisplayName("Deve retornar 409 quando nome duplicado")
    void deveRetornar409QuandoNomeDuplicado() throws Exception {
        // ==========================================
        // ARRANGE: Preparar o cenário com produto pré-existente
        // ==========================================
        ProdutoRequestDTO dtoOriginal = new ProdutoRequestDTO(
                "Feijão Carioca 1kg",
                CategoriaProduto.ALIMENTO,
                10,
                2,
                "kg"
        );
        HttpRequest requestOriginal = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(dtoOriginal)))
                .build();
        httpClient.send(requestOriginal, HttpResponse.BodyHandlers.ofString());

        ProdutoRequestDTO dtoDuplicado = new ProdutoRequestDTO(
                "Feijão Carioca 1kg",
                CategoriaProduto.ALIMENTO,
                50,
                10,
                "kg"
        );
        HttpRequest requestDuplicado = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(dtoDuplicado)))
                .build();

        // ==========================================
        // ACT: Tentar cadastrar com o mesmo nome
        // ==========================================
        HttpResponse<String> response = httpClient.send(requestDuplicado, HttpResponse.BodyHandlers.ofString());

        // ==========================================
        // ASSERT: Verificar o retorno 409 Conflict
        // ==========================================
        Assertions.assertEquals(409, response.statusCode());
        Assertions.assertTrue(response.body().contains("já cadastrado") || response.body().contains("Feijão Carioca 1kg"));
    }

    @Test
    @DisplayName("Deve retornar 422 quando estoque negativo")
    void deveRetornar422QuandoEstoqueNegativo() throws Exception {
        // ==========================================
        // ARRANGE: Preparar o cenário com estoque negativo
        // ==========================================
        ProdutoRequestDTO dtoEstoqueNegativo = new ProdutoRequestDTO(
                "Óleo de Soja 900ml",
                CategoriaProduto.ALIMENTO,
                -10,
                5,
                "unidade"
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(dtoEstoqueNegativo)))
                .build();

        // ==========================================
        // ACT: Tentar cadastrar com estoque negativo
        // ==========================================
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // ==========================================
        // ASSERT: Verificar retorno 422 Unprocessable Entity
        // ==========================================
        Assertions.assertEquals(422, response.statusCode());
        Assertions.assertTrue(response.body().contains("422") || response.body().contains("negativo"));
    }
}
