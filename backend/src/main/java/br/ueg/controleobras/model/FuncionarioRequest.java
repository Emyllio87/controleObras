package br.ueg.controleobras.model;

import br.ueg.controleobras.model.TipoChavePix;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FuncionarioRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 120) String nome,
        String telefone,
        @Size(max = 60) String funcao,
        @NotNull(message = "Valor da diária é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor da diária deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "Valor da diária inválido")
        BigDecimal valorDiaria,
        TipoChavePix tipoChavePix,
        @Size(max = 140) String chavePix,
        @NotBlank(message = "CPF é obrigatório") String cpf) {
}
