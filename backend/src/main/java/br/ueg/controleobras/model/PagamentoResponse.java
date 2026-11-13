package br.ueg.controleobras.model;

import br.ueg.controleobras.model.Pagamento;
import br.ueg.controleobras.model.StatusPagamento;
import br.ueg.controleobras.model.TipoChavePix;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoResponse(
        Long id,
        Long funcionarioId,
        String funcionario,
        LocalDate periodoInicio,
        LocalDate periodoFim,
        BigDecimal valorTotal,
        TipoChavePix tipoChavePix,
        String chavePix,
        StatusPagamento status) {

    public static PagamentoResponse de(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getFuncionario().getId(),
                pagamento.getFuncionario().getNome(),
                pagamento.getPeriodoInicio(),
                pagamento.getPeriodoFim(),
                pagamento.getValorTotal(),
                pagamento.getTipoChavePix(),
                pagamento.getChavePix(),
                pagamento.getStatus());
    }
}
