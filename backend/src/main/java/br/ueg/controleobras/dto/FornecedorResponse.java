package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.Fornecedor;
import br.ueg.controleobras.dominio.TipoChavePix;

public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        String telefone,
        String nomeContato,
        TipoChavePix tipoChavePix,
        String chavePix) {

    public static FornecedorResponse de(Fornecedor fornecedor) {
        return new FornecedorResponse(fornecedor.getId(), fornecedor.getRazaoSocial(), fornecedor.getCnpj(),
                fornecedor.getTelefone(), fornecedor.getNomeContato(), fornecedor.getTipoChavePix(),
                fornecedor.getChavePix());
    }
}
