package br.ueg.controleobras.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemRequest(
        @NotBlank(message = "Descrição do item é obrigatória") @Size(max = 300) String descricao,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
        @Digits(integer = 12, fraction = 3, message = "Quantidade inválida")
        BigDecimal quantidade,
        @NotBlank(message = "Unidade é obrigatória") @Size(max = 20) String unidade) {
}
