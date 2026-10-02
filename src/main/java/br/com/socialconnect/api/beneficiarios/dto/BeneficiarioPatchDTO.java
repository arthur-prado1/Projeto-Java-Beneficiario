package br.com.socialconnect.api.beneficiarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BeneficiarioPatchDTO(
    @Schema(description = "Nome para atualização", example = "Maria da Silva Santos")
    String nome,

    @Schema(description = "Telefone para atualização", example = "11988887777")
    String telefone,

    @Schema(description = "Endereço para atualização", example = "Rua Nova, 456")
    String endereco,

    @Schema(description = "Situação de vulnerabilidade para atualização", example = "Renda estável")
    String situacaoVulnerabilidade
) {}
