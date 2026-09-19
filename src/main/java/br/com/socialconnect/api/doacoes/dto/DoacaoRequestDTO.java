package br.com.socialconnect.api.doacoes.dto;

import br.com.socialconnect.api.doacoes.model.TipoDoacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DoacaoRequestDTO(
    Long idDoador,
    LocalDate dataDoacao,
    BigDecimal valor,
    TipoDoacao tipo
) {}
