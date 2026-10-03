package br.com.socialconnect.api.produtos.dto;

import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ProdutoResponseDTO(
    @Schema(description = "Identificador único do produto", example = "1")
    Long idProduto,

    @Schema(description = "Nome do produto", example = "Arroz 5kg")
    String nome,

    @Schema(description = "Categoria do produto", example = "ALIMENTO")
    CategoriaProduto categoria,

    @Schema(description = "Quantidade atual em estoque", example = "3")
    Integer estoqueAtual,

    @Schema(description = "Quantidade mínima de segurança em estoque", example = "10")
    Integer estoqueMinimo,

    @Schema(description = "Unidade de medida do produto", example = "unidade")
    String unidadeMedida,

    @Schema(description = "Data de realização do cadastro do produto", example = "2026-09-18")
    LocalDate dataCadastro,

    @Schema(description = "Indica se o estoque atual está abaixo do estoque mínimo exigido", example = "true")
    Boolean estoqueBaixo
) {}
