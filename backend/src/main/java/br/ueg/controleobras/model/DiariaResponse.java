package br.ueg.controleobras.model;

import br.ueg.controleobras.model.Diaria;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiariaResponse(Long id, LocalDate data, BigDecimal valor) {

    public static DiariaResponse de(Diaria diaria) {
        return new DiariaResponse(diaria.getId(), diaria.getData(), diaria.getValor());
    }
}
