package br.com.socialconnect.api.beneficiarios.controller;

import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioRequestDTO;
import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioResponseDTO;
import br.com.socialconnect.api.beneficiarios.service.BeneficiarioService;
import br.com.socialconnect.api.exception.CpfDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeneficiarioController.class)
@Import({br.com.socialconnect.api.exception.GlobalExceptionHandler.class, br.com.socialconnect.api.config.I18nConfig.class})
class BeneficiarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private BeneficiarioService service;

    @Test
    @DisplayName("POST /api/v1/beneficiarios deve retornar 201 Created quando válido")
    void deveRetornar201QuandoCriarValido() throws Exception {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        BeneficiarioRequestDTO request = new BeneficiarioRequestDTO(
                "Carlos Silva", "12345678901", "11987654321",
                "Rua A, 100", "Vulnerável"
        );
        BeneficiarioResponseDTO response = new BeneficiarioResponseDTO(
                1L, "Carlos Silva", "12345678901", "11987654321",
                "Rua A, 100", "Vulnerável", LocalDate.now()
        );

        Mockito.when(service.criar(Mockito.any(BeneficiarioRequestDTO.class))).thenReturn(response);

        // ==========================================
        // ACT & ASSERT: Executar requisição e validar resposta
        // ==========================================
        mockMvc.perform(post("/api/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/beneficiarios/1"))
                .andExpect(jsonPath("$.idBeneficiario").value(1))
                .andExpect(jsonPath("$.nome").value("Carlos Silva"));
    }

    @Test
    @DisplayName("POST /api/v1/beneficiarios deve retornar 400 Bad Request com Problem Details quando inválido")
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        // ==========================================
        // ARRANGE: Preparar dados inválidos (nome vazio e CPF incorreto)
        // ==========================================
        BeneficiarioRequestDTO request = new BeneficiarioRequestDTO(
                "", "invalido", "11987654321", "Rua A", "Vulnerável"
        );

        // ==========================================
        // ACT & ASSERT: Executar requisição e validar Problem Details
        // ==========================================
        mockMvc.perform(post("/api/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/beneficiarios deve retornar 409 Conflict quando CPF duplicado")
    void deveRetornar409QuandoCpfDuplicado() throws Exception {
        // ==========================================
        // ARRANGE: Preparar o cenário com CPF duplicado
        // ==========================================
        BeneficiarioRequestDTO request = new BeneficiarioRequestDTO(
                "Carlos Silva", "12345678901", "11987654321",
                "Rua A, 100", "Vulnerável"
        );

        Mockito.when(service.criar(Mockito.any(BeneficiarioRequestDTO.class)))
                .thenThrow(new CpfDuplicadoException("12345678901"));

        // ==========================================
        // ACT & ASSERT: Executar requisição e validar resposta 409 Conflict
        // ==========================================
        mockMvc.perform(post("/api/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("CPF já cadastrado"));
    }

    @Test
    @DisplayName("GET /api/v1/beneficiarios/{id} deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoNaoEncontrado() throws Exception {
        // ==========================================
        // ARRANGE: Simular recurso inexistente
        // ==========================================
        Mockito.when(service.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Beneficiário não encontrado com o ID: 999"));

        // ==========================================
        // ACT & ASSERT: Executar requisição e validar resposta 404 Not Found
        // ==========================================
        mockMvc.perform(get("/api/v1/beneficiarios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }
}
