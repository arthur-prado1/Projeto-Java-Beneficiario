package br.com.socialconnect.api.produtos.dto;

import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.validation.EstoqueNaoNegativo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoRequestDTO(
    @Schema(description = "Nome do produto", example = "Arroz 5kg")
    @NotBlank(message = "{produto.nome.obrigatorio}")
    @Size(max = 150, message = "{produto.nome.tamanho}")
    String nome,

    @Schema(description = "Categoria do produto", example = "ALIMENTO")
    @NotNull(message = "{produto.categoria.obrigatoria}")
    CategoriaProduto categoria,

    @Schema(description = "Quantidade atual em estoque", example = "3")
    @NotNull(message = "{produto.estoque.obrigatorio}")
    @EstoqueNaoNegativo
    @Min(value = 0, message = "{produto.estoque.minimo}")
    Integer estoqueAtual,

    @Schema(description = "Quantidade mínima de segurança em estoque", example = "10")
    @NotNull(message = "{produto.estoqueMinimo.obrigatorio}")
    @EstoqueNaoNegativo
    @Min(value = 0, message = "{produto.estoqueMinimo.minimo}")
    Integer estoqueMinimo,

    @Schema(description = "Unidade de medida do produto", example = "unidade")
    @NotBlank(message = "{produto.unidadeMedida.obrigatoria}")
    @Size(max = 20, message = "{produto.unidadeMedida.tamanho}")
    String unidadeMedida
) {}
