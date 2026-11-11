package br.ueg.controleobras.dominio;

public enum TipoChavePix {

    CPF("CPF"),
    CNPJ("CNPJ"),
    TELEFONE("Telefone"),
    EMAIL("E-mail"),
    ALEATORIA("Chave aleatória");

    private final String descricao;

    TipoChavePix(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
