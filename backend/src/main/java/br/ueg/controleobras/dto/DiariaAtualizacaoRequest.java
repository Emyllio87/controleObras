package br.ueg.controleobras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiariaAtualizacaoRequest(
        @NotNull(message = "Data é obrigatória") LocalDate data,
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "Valor inválido")
        BigDecimal valor) {
}
