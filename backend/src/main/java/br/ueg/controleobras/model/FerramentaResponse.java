package br.ueg.controleobras.model;

import br.ueg.controleobras.model.Ferramenta;

import java.math.BigDecimal;

public record FerramentaResponse(Long id, String nome, BigDecimal quantidade, String unidade) {

    public static FerramentaResponse de(Ferramenta ferramenta) {
        return new FerramentaResponse(ferramenta.getId(), ferramenta.getNome(),
                ferramenta.getQuantidade(), ferramenta.getUnidade());
    }
}
