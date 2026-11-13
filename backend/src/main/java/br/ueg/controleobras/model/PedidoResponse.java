package br.ueg.controleobras.model;

import br.ueg.controleobras.model.PedidoOrcamento;

import java.time.LocalDate;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long numero,
        LocalDate dataEmissao,
        FornecedorResponse fornecedor,
        List<ItemResponse> itens) {

    public static PedidoResponse de(PedidoOrcamento pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getDataEmissao(),
                FornecedorResponse.de(pedido.getFornecedor()),
                pedido.getItens().stream().map(ItemResponse::de).toList());
    }
}
