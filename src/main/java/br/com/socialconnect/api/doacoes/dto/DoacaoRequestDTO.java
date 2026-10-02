package br.com.socialconnect.api.doacoes.dto;

import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DoacaoRequestDTO(
    @Schema(description = "Identificador do doador", example = "1")
    @NotNull(message = "ID do doador é obrigatório")
    Long idDoador,

    @Schema(description = "Data da doação (não pode estar no futuro)", example = "2026-09-11")
    @NotNull(message = "Data da doação é obrigatória")
    @PastOrPresent(message = "A data da doação não pode estar no futuro")
    LocalDate dataDoacao,

    @Schema(description = "Valor estimado ou monetário da doação", example = "100.00")
    @Positive(message = "Valor deve ser positivo")
    BigDecimal valor,

    @Schema(description = "Tipo da doação", example = "ALIMENTO")
    @NotNull(message = "Tipo de doação é obrigatório")
    TipoDoacao tipo,

    @Schema(description = "Descrição detalhada dos itens doados", example = "Cesta básica completa")
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao
) {
    public DoacaoRequestDTO(Long idDoador, LocalDate dataDoacao, BigDecimal valor, TipoDoacao tipo) {
        this(idDoador, dataDoacao, valor, tipo, null);
    }
}
