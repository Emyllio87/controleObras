package br.ueg.controleobras.dominio;

public enum Tela {

    ADMINISTRADOR("Administrador"),
    FUNCIONARIOS("Funcionários"),
    FORNECEDORES("Fornecedores"),
    PEDIDOS_ORCAMENTO("Pedidos de orçamento"),
    AUTORIZACAO_PAGAMENTO("Autorização de pagamento"),
    ALMOXARIFADO("Almoxarifado"),
    DIARIAS("Diárias"),
    PAGAMENTOS("Pagamentos"),
    HISTORICO_DIARIAS("Histórico de diárias");

    private final String descricao;

    Tela(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
