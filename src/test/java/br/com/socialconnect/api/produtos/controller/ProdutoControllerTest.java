package br.com.socialconnect.api.produtos.controller;

import br.com.socialconnect.api.exception.EstoqueNegativoException;
import br.com.socialconnect.api.exception.GlobalExceptionHandler;
import br.com.socialconnect.api.exception.ProdutoNomeDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.service.ProdutoService;
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

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class)
@Import({GlobalExceptionHandler.class, br.com.socialconnect.api.config.I18nConfig.class})
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ProdutoService service;

    @Test
    @DisplayName("GET /api/v1/produtos deve retornar 200 OK com lista paginada")
    void deveRetornar200AoListarProdutos() throws Exception {
        ProdutoResponseDTO item = new ProdutoResponseDTO(
                1L, "Arroz 5kg", CategoriaProduto.ALIMENTO, 10, 5, "unidade", LocalDate.now(), false
        );
        Mockito.when(service.listar(Mockito.any(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        mockMvc.perform(get("/api/v1/produtos")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoria", "ALIMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Arroz 5kg"))
                .andExpect(jsonPath("$.content[0].categoria").value("ALIMENTO"));
    }

    @Test
    @DisplayName("GET /api/v1/produtos/{id_produto} deve retornar 200 OK quando encontrado")
    void deveRetornar200AoBuscarPorId() throws Exception {
        ProdutoResponseDTO response = new ProdutoResponseDTO(
                1L, "Feijão 1kg", CategoriaProduto.ALIMENTO, 3, 10, "kg", LocalDate.now(), true
        );
        Mockito.when(service.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProduto").value(1))
                .andExpect(jsonPath("$.nome").value("Feijão 1kg"))
                .andExpect(jsonPath("$.estoqueBaixo").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/produtos/{id_produto} deve retornar 404 Not Found quando não encontrado")
    void deveRetornar404QuandoProdutoNaoEncontrado() throws Exception {
        Mockito.when(service.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado com o ID: 999"));

        mockMvc.perform(get("/api/v1/produtos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    @DisplayName("POST /api/v1/produtos deve retornar 201 Created com cabeçalho Location")
    void deveRetornar201AoCriarProdutoValido() throws Exception {
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Arroz 5kg", CategoriaProduto.ALIMENTO, 3, 10, "unidade"
        );
        ProdutoResponseDTO response = new ProdutoResponseDTO(
                1L, "Arroz 5kg", CategoriaProduto.ALIMENTO, 3, 10, "unidade", LocalDate.now(), true
        );

        Mockito.when(service.criar(Mockito.any(ProdutoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/produtos/1"))
                .andExpect(jsonPath("$.idProduto").value(1))
                .andExpect(jsonPath("$.nome").value("Arroz 5kg"))
                .andExpect(jsonPath("$.estoqueBaixo").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/produtos deve retornar 400 Bad Request quando dados inválidos")
    void deveRetornar400AoCriarComDadosInvalidos() throws Exception {
        // Nome em branco e unidade de medida em branco
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "", CategoriaProduto.ALIMENTO, 10, 5, ""
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Erro de validação"));
    }

    @Test
    @DisplayName("POST /api/v1/produtos deve retornar 409 Conflict quando nome duplicado")
    void deveRetornar409AoCriarComNomeDuplicado() throws Exception {
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Arroz 5kg", CategoriaProduto.ALIMENTO, 10, 5, "unidade"
        );

        Mockito.when(service.criar(Mockito.any(ProdutoRequestDTO.class)))
                .thenThrow(new ProdutoNomeDuplicadoException("Arroz 5kg"));

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Nome de produto já cadastrado"));
    }

    @Test
    @DisplayName("POST /api/v1/produtos deve retornar 422 Unprocessable Entity quando estoque negativo")
    void deveRetornar422AoCriarComEstoqueNegativo() throws Exception {
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Arroz 5kg", CategoriaProduto.ALIMENTO, -5, 5, "unidade"
        );

        mockMvc.perform(post("/api/v1/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title").value("Estoque inválido"));
    }

    @Test
    @DisplayName("PUT /api/v1/produtos/{id_produto} deve retornar 200 OK quando atualização com sucesso")
    void deveRetornar200AoAtualizarProduto() throws Exception {
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Arroz 5kg Branco", CategoriaProduto.ALIMENTO, 20, 10, "unidade"
        );
        ProdutoResponseDTO response = new ProdutoResponseDTO(
                1L, "Arroz 5kg Branco", CategoriaProduto.ALIMENTO, 20, 10, "unidade", LocalDate.now(), false
        );

        Mockito.when(service.atualizar(Mockito.eq(1L), Mockito.any(ProdutoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProduto").value(1))
                .andExpect(jsonPath("$.nome").value("Arroz 5kg Branco"))
                .andExpect(jsonPath("$.estoqueBaixo").value(false));
    }

    @Test
    @DisplayName("PUT /api/v1/produtos/{id_produto} deve retornar 404 Not Found quando produto não existe")
    void deveRetornar404AoAtualizarProdutoInexistente() throws Exception {
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Arroz 5kg", CategoriaProduto.ALIMENTO, 20, 10, "unidade"
        );

        Mockito.when(service.atualizar(Mockito.eq(999L), Mockito.any(ProdutoRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado com o ID: 999"));

        mockMvc.perform(put("/api/v1/produtos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/v1/produtos/{id_produto} deve retornar 204 No Content quando sucesso")
    void deveRetornar204AoDeletarProduto() throws Exception {
        Mockito.doNothing().when(service).deletar(1L);

        mockMvc.perform(delete("/api/v1/produtos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/produtos/{id_produto} deve retornar 404 Not Found quando produto não existe")
    void deveRetornar404AoDeletarProdutoInexistente() throws Exception {
        Mockito.doThrow(new RecursoNaoEncontradoException("Produto não encontrado com o ID: 999"))
                .when(service).deletar(999L);

        mockMvc.perform(delete("/api/v1/produtos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
