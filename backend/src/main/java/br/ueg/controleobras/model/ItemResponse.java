package br.ueg.controleobras.model;

import br.ueg.controleobras.model.ItemPedido;

import java.math.BigDecimal;

public record ItemResponse(Long id, Integer numero, String descricao, BigDecimal quantidade, String unidade) {

    public static ItemResponse de(ItemPedido item) {
        return new ItemResponse(item.getId(), item.getNumero(), item.getDescricao(),
                item.getQuantidade(), item.getUnidade());
    }
}
