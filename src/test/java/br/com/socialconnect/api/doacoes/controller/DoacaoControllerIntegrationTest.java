package br.com.socialconnect.api.doacoes.controller;

import br.com.socialconnect.api.doacoes.dto.DoacaoRequestDTO;
import br.com.socialconnect.api.doacoes.dto.DoacaoResponseDTO;
import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import br.com.socialconnect.api.doadores.model.Doador;
import br.com.socialconnect.api.doadores.model.TipoDoador;
import br.com.socialconnect.api.doadores.repository.DoadorRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class DoacaoControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DoadorRepository doadorRepository;

    private Long idDoadorCriado;

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
    void deveCriarDoacaoQuandoDadosValidos() {
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

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        ResponseEntity<DoacaoResponseDTO> resposta = restTemplate.postForEntity(
                "/api/v1/doacoes", dto, DoacaoResponseDTO.class
        );

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        Assertions.assertNotNull(resposta.getBody());
        Assertions.assertNotNull(resposta.getBody().idDoacao(), "ID da doação não deve ser nulo");
        Assertions.assertEquals(new BigDecimal("150.00"), resposta.getBody().valor());
    }

    @Test
    @DisplayName("Deve retornar 400 quando data futura")
    void deveRetornar400QuandoDataFutura() {
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

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        ResponseEntity<String> resposta = restTemplate.postForEntity(
                "/api/v1/doacoes", dto, String.class
        );

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        Assertions.assertTrue(
                resposta.getBody() != null && resposta.getBody().toLowerCase().contains("futuro"),
                "A resposta deve conter mensagem sobre data no futuro"
        );
    }
}
