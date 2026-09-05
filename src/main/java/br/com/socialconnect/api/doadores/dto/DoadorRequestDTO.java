package br.com.socialconnect.api.doadores.dto;

import br.com.socialconnect.api.doadores.model.TipoDoador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DoadorRequestDTO(
    @Schema(description = "Nome do doador ou razão social", example = "Instituto Esperança")
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String nome,

    @Schema(description = "Tipo de doador (PESSOA_FISICA ou PESSOA_JURIDICA)", example = "PESSOA_JURIDICA")
    @NotNull(message = "Tipo é obrigatório")
    TipoDoador tipo,

    @Schema(description = "E-mail de contato", example = "contato@esperanca.org")
    @Email(message = "E-mail com formato inválido")
    @Size(max = 100, message = "E-mail deve ter no máximo 100 caracteres")
    String email,

    @Schema(description = "Telefone de contato", example = "1133334444")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    String telefone
) {}
