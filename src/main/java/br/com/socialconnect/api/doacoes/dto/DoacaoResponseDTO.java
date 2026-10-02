package br.com.socialconnect.api.doacoes.dto;

import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DoacaoResponseDTO(
    @Schema(description = "Identificador único da doação", example = "1")
    Long idDoacao,

    @Schema(description = "Identificador do doador", example = "1")
    Long idDoador,

    @Schema(description = "Data em que a doação foi realizada", example = "2026-09-11")
    LocalDate dataDoacao,

    @Schema(description = "Valor estimado ou monetário da doação", example = "100.00")
    BigDecimal valor,

    @Schema(description = "Tipo da doação", example = "ALIMENTO")
    TipoDoacao tipo,

    @Schema(description = "Descrição dos itens ou detalhes da doação", example = "Cesta básica completa")
    String descricao
) {
    public DoacaoResponseDTO(Long idDoacao, Long idDoador, LocalDate dataDoacao, BigDecimal valor, TipoDoacao tipo) {
        this(idDoacao, idDoador, dataDoacao, valor, tipo, null);
    }
}
