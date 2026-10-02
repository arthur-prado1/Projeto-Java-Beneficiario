package br.com.socialconnect.api.beneficiarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BeneficiarioRequestDTO(
    @Schema(description = "Nome completo do beneficiário", example = "Maria da Silva")
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String nome,

    @Schema(description = "CPF sem formatação ou formatado", example = "12345678900")
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}|\\d{14}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve ter 11 ou 14 dígitos")
    String cpf,

    @Schema(description = "Telefone de contato", example = "11999999999")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    String telefone,

    @Schema(description = "Endereço residencial", example = "Rua das Flores, 123")
    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    String endereco,

    @Schema(description = "Descrição da situação de vulnerabilidade social", example = "Renda familiar baixa")
    @Size(max = 500, message = "Situação deve ter no máximo 500 caracteres")
    String situacaoVulnerabilidade
) {}
