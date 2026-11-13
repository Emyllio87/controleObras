package br.ueg.controleobras.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PedidoRequest(
        @NotNull(message = "Selecione o fornecedor") Long fornecedorId,
        LocalDate dataEmissao,
        @NotEmpty(message = "Inclua ao menos um item") @Valid List<ItemRequest> itens) {
}
