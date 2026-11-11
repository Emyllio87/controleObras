package br.ueg.controleobras.dto;

import java.math.BigDecimal;
import java.util.List;

public record CardDiariaResponse(
        Long funcionarioId,
        String nome,
        String funcao,
        BigDecimal valorDiaria,
        BigDecimal total,
        List<DiariaResponse> diarias) {
}
