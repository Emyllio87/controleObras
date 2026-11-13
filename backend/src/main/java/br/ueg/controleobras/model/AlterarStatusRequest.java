package br.ueg.controleobras.model;

import br.ueg.controleobras.model.StatusPagamento;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
        @NotNull(message = "Informe o status") StatusPagamento status) {
}
