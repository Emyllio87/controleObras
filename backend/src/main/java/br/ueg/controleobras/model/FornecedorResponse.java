package br.ueg.controleobras.model;

import br.ueg.controleobras.model.Fornecedor;
import br.ueg.controleobras.model.TipoChavePix;

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
