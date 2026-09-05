package br.com.socialconnect.api.doadores.dto;

import br.com.socialconnect.api.doadores.model.TipoDoador;
import io.swagger.v3.oas.annotations.media.Schema;

public record DoadorResponseDTO(
    @Schema(description = "Identificador único do doador", example = "1")
    Long idDoador,

    @Schema(description = "Nome do doador ou razão social", example = "Instituto Esperança")
    String nome,

    @Schema(description = "Tipo de doador", example = "PESSOA_JURIDICA")
    TipoDoador tipo,

    @Schema(description = "E-mail de contato", example = "contato@esperanca.org")
    String email,

    @Schema(description = "Telefone de contato", example = "1133334444")
    String telefone
) {}
