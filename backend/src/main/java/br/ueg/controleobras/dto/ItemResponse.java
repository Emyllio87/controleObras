package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.ItemPedido;

import java.math.BigDecimal;

public record ItemResponse(Long id, Integer numero, String descricao, BigDecimal quantidade, String unidade) {

    public static ItemResponse de(ItemPedido item) {
        return new ItemResponse(item.getId(), item.getNumero(), item.getDescricao(),
                item.getQuantidade(), item.getUnidade());
    }
}
