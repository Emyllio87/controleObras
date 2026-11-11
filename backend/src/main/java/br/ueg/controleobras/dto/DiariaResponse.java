package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.Diaria;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiariaResponse(Long id, LocalDate data, BigDecimal valor) {

    public static DiariaResponse de(Diaria diaria) {
        return new DiariaResponse(diaria.getId(), diaria.getData(), diaria.getValor());
    }
}
