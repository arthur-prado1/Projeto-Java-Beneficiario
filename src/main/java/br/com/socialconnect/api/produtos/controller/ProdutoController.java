package br.com.socialconnect.api.produtos.controller;

import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/produtos")
@Tag(name = "Produtos", description = "API para gestão e controle de estoque de produtos da instituição")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Lista todos os produtos com paginação",
            description = "Retorna uma lista paginada de produtos com suporte a filtros opcionais por nome parcial e categoria"
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada de produtos retornada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de paginação ou filtro inválidos")
    public ResponseEntity<Page<ProdutoResponseDTO>> listar(
            @Parameter(description = "Filtro parcial por nome do produto", example = "Arroz")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Filtro por categoria do produto", example = "ALIMENTO")
            @RequestParam(required = false) CategoriaProduto categoria,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(nome, categoria, pageable));
    }

    @GetMapping("/{id_produto}")
    @Operation(
            summary = "Busca produto por ID",
            description = "Recupera os detalhes completos de um produto a partir de seu identificador"
    )
    @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único do produto", example = "1")
            @PathVariable("id_produto") Long idProduto) {
        return ResponseEntity.ok(service.buscarPorId(idProduto));
    }

    @PostMapping
    @Operation(
            summary = "Cadastra um novo produto",
            description = "Cria um produto no estoque com validação de dados, unicidade de nome e regra de estoque não negativo"
    )
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
    @ApiResponse(responseCode = "409", description = "Já existe um produto com o nome informado")
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada (estoque não pode ser negativo)")
    public ResponseEntity<ProdutoResponseDTO> criar(
            @Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO salvo = service.criar(dto);
        URI location = URI.create("/api/v1/produtos/" + salvo.idProduto());
        return ResponseEntity.created(location).body(salvo);
    }

    @PutMapping("/{id_produto}")
    @Operation(
            summary = "Atualização total do produto",
            description = "Substitui todos os dados do produto informado pelo ID (PUT)"
    )
    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @ApiResponse(responseCode = "409", description = "Nome informado já pertence a outro produto")
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada (estoque não pode ser negativo)")
    public ResponseEntity<ProdutoResponseDTO> atualizar(
            @Parameter(description = "Identificador único do produto", example = "1")
            @PathVariable("id_produto") Long idProduto,
            @Valid @RequestBody ProdutoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(idProduto, dto));
    }

    @DeleteMapping("/{id_produto}")
    @Operation(
            summary = "Remove um produto",
            description = "Exclui definitivamente o registro de um produto pelo ID"
    )
    @ApiResponse(responseCode = "204", description = "Produto excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador único do produto", example = "1")
            @PathVariable("id_produto") Long idProduto) {
        service.deletar(idProduto);
        return ResponseEntity.noContent().build();
    }
}
