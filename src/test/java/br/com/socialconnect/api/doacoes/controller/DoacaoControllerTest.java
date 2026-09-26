package br.com.socialconnect.api.doacoes.controller;

import br.com.socialconnect.api.doacoes.dto.DoacaoRequestDTO;
import br.com.socialconnect.api.doacoes.dto.DoacaoResponseDTO;
import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import br.com.socialconnect.api.doacoes.service.DoacaoService;
import br.com.socialconnect.api.exception.GlobalExceptionHandler;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoacaoController.class)
@Import({GlobalExceptionHandler.class, br.com.socialconnect.api.config.I18nConfig.class})
class DoacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private DoacaoService service;

    @Test
    @DisplayName("POST /api/v1/doacoes deve retornar 201 Created quando válido")
    void deveRetornar201QuandoCriarDoacaoValida() throws Exception {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        DoacaoRequestDTO request = new DoacaoRequestDTO(
                1L, LocalDate.of(2026, 9, 11), new BigDecimal("100.00"),
                TipoDoacao.ALIMENTO, "Cesta básica"
        );
        DoacaoResponseDTO response = new DoacaoResponseDTO(
                1L, 1L, LocalDate.of(2026, 9, 11), new BigDecimal("100.00"),
                TipoDoacao.ALIMENTO, "Cesta básica"
        );

        Mockito.when(service.criar(Mockito.any(DoacaoRequestDTO.class))).thenReturn(response);

        // ==========================================
        // ACT & ASSERT: Executar requisição e validar resposta 201
        // ==========================================
        mockMvc.perform(post("/api/v1/doacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/doacoes/1"))
                .andExpect(jsonPath("$.idDoacao").value(1))
                .andExpect(jsonPath("$.valor").value(100.00));
    }

    @Test
    @DisplayName("POST /api/v1/doacoes deve retornar 400 Bad Request quando data da doação for no futuro")
    void deveRetornar400QuandoDataFutura() throws Exception {
        // ==========================================
        // ARRANGE: Preparar data no futuro
        // ==========================================
        DoacaoRequestDTO request = new DoacaoRequestDTO(
                1L, LocalDate.now().plusDays(10), new BigDecimal("100.00"),
                TipoDoacao.ALIMENTO, "Cesta básica"
        );

        // ==========================================
        // ACT & ASSERT: Executar requisição e validar Problem Details com erro de data no futuro
        // ==========================================
        mockMvc.perform(post("/api/v1/doacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Erro de validação"));
    }

    @Test
    @DisplayName("POST /api/v1/doacoes deve retornar 404 Not Found quando doador não existir")
    void deveRetornar404QuandoDoadorNaoExiste() throws Exception {
        // ==========================================
        // ARRANGE: Preparar cenário de doador inexistente
        // ==========================================
        DoacaoRequestDTO request = new DoacaoRequestDTO(
                999L, LocalDate.of(2026, 9, 11), new BigDecimal("100.00"),
                TipoDoacao.ALIMENTO, "Cesta básica"
        );

        Mockito.when(service.criar(Mockito.any(DoacaoRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Doador não encontrado com o ID: 999"));

        // ==========================================
        // ACT & ASSERT: Executar requisição e validar resposta 404
        // ==========================================
        mockMvc.perform(post("/api/v1/doacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/doacoes deve retornar 200 OK com paginação")
    void deveRetornar200AoListarComPaginacao() throws Exception {
        // ==========================================
        // ARRANGE: Preparar página mockada
        // ==========================================
        DoacaoResponseDTO item = new DoacaoResponseDTO(
                1L, 1L, LocalDate.now(), new BigDecimal("50.00"), TipoDoacao.ROUPA, "Roupas de frio"
        );
        Mockito.when(service.listar(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        // ==========================================
        // ACT & ASSERT: Executar GET e validar estrutura da resposta
        // ==========================================
        mockMvc.perform(get("/api/v1/doacoes?tipo=ROUPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].idDoacao").value(1));
    }
}
