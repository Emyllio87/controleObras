package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.TipoChavePix;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank(message = "Razão social é obrigatória") @Size(max = 150) String razaoSocial,
        @NotBlank(message = "CNPJ é obrigatório") String cnpj,
        String telefone,
        @Size(max = 120) String nomeContato,
        TipoChavePix tipoChavePix,
        @Size(max = 140) String chavePix) {
}
