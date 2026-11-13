package br.ueg.controleobras.model;

import br.ueg.controleobras.model.Funcionario;
import br.ueg.controleobras.model.TipoChavePix;

import java.math.BigDecimal;

public record FuncionarioResponse(
        Long id,
        String nome,
        String telefone,
        String funcao,
        BigDecimal valorDiaria,
        TipoChavePix tipoChavePix,
        String chavePix,
        String cpf) {

    public static FuncionarioResponse de(Funcionario funcionario) {
        return new FuncionarioResponse(funcionario.getId(), funcionario.getNome(), funcionario.getTelefone(),
                funcionario.getFuncao(), funcionario.getValorDiaria(), funcionario.getTipoChavePix(),
                funcionario.getChavePix(), funcionario.getCpf());
    }
}
