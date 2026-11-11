package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.Tela;

public record TelaResponse(String chave, String descricao) {

    public static TelaResponse de(Tela tela) {
        return new TelaResponse(tela.name(), tela.getDescricao());
    }
}
