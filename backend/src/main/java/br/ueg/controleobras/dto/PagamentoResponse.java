package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.Pagamento;
import br.ueg.controleobras.dominio.StatusPagamento;
import br.ueg.controleobras.dominio.TipoChavePix;

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
