package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.FechamentoDiaria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FechamentoResponse(
        Long id,
        Long funcionarioId,
        String funcionario,
        LocalDate periodoInicio,
        LocalDate periodoFim,
        BigDecimal valorTotal,
        LocalDate dataFechamento,
        List<DiariaResponse> diarias) {

    public static FechamentoResponse de(FechamentoDiaria fechamento) {
        return new FechamentoResponse(
                fechamento.getId(),
                fechamento.getFuncionario().getId(),
                fechamento.getFuncionario().getNome(),
                fechamento.getPeriodoInicio(),
                fechamento.getPeriodoFim(),
                fechamento.getValorTotal(),
                fechamento.getDataFechamento(),
                fechamento.getDiarias().stream().map(DiariaResponse::de).toList());
    }
}
