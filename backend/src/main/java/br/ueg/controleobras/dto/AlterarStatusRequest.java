package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.StatusPagamento;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
        @NotNull(message = "Informe o status") StatusPagamento status) {
}
