package br.ueg.controleobras.model;

import br.ueg.controleobras.model.AutorizacaoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AutorizacaoResponse(
        Long id,
        Long numero,
        Long pedidoId,
        Long pedidoNumero,
        String fornecedor,
        BigDecimal valor,
        LocalDate dataEmissao,
        String arquivoNome,
        String arquivoTipo,
        Long arquivoTamanho,
        boolean possuiAnexo) {

    public static AutorizacaoResponse de(AutorizacaoPagamento autorizacao) {
        return new AutorizacaoResponse(
                autorizacao.getId(),
                autorizacao.getNumero(),
                autorizacao.getPedido().getId(),
                autorizacao.getPedido().getNumero(),
                autorizacao.getPedido().getFornecedor().getRazaoSocial(),
                autorizacao.getValor(),
                autorizacao.getDataEmissao(),
                autorizacao.getArquivoNome(),
                autorizacao.getArquivoTipo(),
                autorizacao.getArquivoTamanho(),
                autorizacao.getArquivoCaminho() != null);
    }
}
