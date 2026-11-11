package br.ueg.controleobras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FerramentaRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 150) String nome,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
        @Digits(integer = 12, fraction = 3, message = "Quantidade inválida")
        BigDecimal quantidade,
        @NotBlank(message = "Unidade é obrigatória") @Size(max = 20) String unidade) {
}
