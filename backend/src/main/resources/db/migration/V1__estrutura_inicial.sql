CREATE TABLE usuarios (
    id        BIGSERIAL PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    login     VARCHAR(60)  NOT NULL UNIQUE,
    senha     VARCHAR(100) NOT NULL,
    status    VARCHAR(10)  NOT NULL DEFAULT 'ATIVO'
);

CREATE TABLE usuario_telas (
    usuario_id BIGINT      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    tela       VARCHAR(40) NOT NULL,
    PRIMARY KEY (usuario_id, tela)
);

CREATE TABLE funcionarios (
    id           BIGSERIAL PRIMARY KEY,
    nome         VARCHAR(120)  NOT NULL,
    telefone     VARCHAR(11),
    funcao       VARCHAR(60),
    valor_diaria NUMERIC(15, 2) NOT NULL,
    tipo_chave_pix VARCHAR(20),
    chave_pix    VARCHAR(140),
    cpf          VARCHAR(11)   NOT NULL UNIQUE
);

CREATE TABLE fornecedores (
    id             BIGSERIAL PRIMARY KEY,
    razao_social   VARCHAR(150) NOT NULL,
    cnpj           VARCHAR(14)  NOT NULL UNIQUE,
    telefone       VARCHAR(11),
    nome_contato   VARCHAR(120),
    tipo_chave_pix VARCHAR(20),
    chave_pix      VARCHAR(140)
);

CREATE SEQUENCE pedido_numero_seq START 1;

CREATE TABLE pedidos_orcamento (
    id           BIGSERIAL PRIMARY KEY,
    numero       BIGINT       NOT NULL UNIQUE DEFAULT nextval('pedido_numero_seq'),
    descricao    VARCHAR(500) NOT NULL,
    data_emissao DATE         NOT NULL,
    fornecedor_id BIGINT      NOT NULL REFERENCES fornecedores (id)
);

CREATE TABLE pedido_itens (
    id         BIGSERIAL PRIMARY KEY,
    pedido_id  BIGINT        NOT NULL REFERENCES pedidos_orcamento (id) ON DELETE CASCADE,
    numero     INTEGER       NOT NULL,
    descricao  VARCHAR(300)  NOT NULL,
    quantidade NUMERIC(15, 3) NOT NULL,
    unidade    VARCHAR(20)   NOT NULL,
    CONSTRAINT uk_item_pedido_numero UNIQUE (pedido_id, numero),
    CONSTRAINT ck_item_quantidade_positiva CHECK (quantidade > 0)
);

CREATE SEQUENCE autorizacao_numero_seq START 1;

CREATE TABLE autorizacoes_pagamento (
    id              BIGSERIAL PRIMARY KEY,
    numero          BIGINT        NOT NULL UNIQUE DEFAULT nextval('autorizacao_numero_seq'),
    pedido_id       BIGINT        NOT NULL UNIQUE REFERENCES pedidos_orcamento (id),
    valor           NUMERIC(15, 2) NOT NULL,
    data_emissao    DATE          NOT NULL,
    arquivo_nome    VARCHAR(255),
    arquivo_caminho VARCHAR(500),
    arquivo_tipo    VARCHAR(80),
    arquivo_tamanho BIGINT,
    CONSTRAINT ck_autorizacao_valor_positivo CHECK (valor > 0)
);

CREATE TABLE ferramentas (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(150)  NOT NULL,
    quantidade NUMERIC(15, 3) NOT NULL,
    unidade    VARCHAR(20)   NOT NULL,
    CONSTRAINT ck_ferramenta_quantidade_positiva CHECK (quantidade > 0)
);

CREATE TABLE fechamentos_diarias (
    id              BIGSERIAL PRIMARY KEY,
    funcionario_id  BIGINT        NOT NULL REFERENCES funcionarios (id),
    periodo_inicio  DATE          NOT NULL,
    periodo_fim     DATE          NOT NULL,
    valor_total     NUMERIC(15, 2) NOT NULL,
    data_fechamento DATE          NOT NULL
);

CREATE TABLE diarias (
    id            BIGSERIAL PRIMARY KEY,
    funcionario_id BIGINT        NOT NULL REFERENCES funcionarios (id),
    data          DATE          NOT NULL,
    valor         NUMERIC(15, 2) NOT NULL,
    fechamento_id BIGINT REFERENCES fechamentos_diarias (id),
    CONSTRAINT ck_diaria_valor_positivo CHECK (valor > 0)
);

CREATE INDEX ix_diarias_funcionario ON diarias (funcionario_id);
CREATE INDEX ix_diarias_fechamento ON diarias (fechamento_id);

CREATE TABLE pagamentos (
    id              BIGSERIAL PRIMARY KEY,
    fechamento_id   BIGINT        NOT NULL UNIQUE REFERENCES fechamentos_diarias (id),
    funcionario_id  BIGINT        NOT NULL REFERENCES funcionarios (id),
    periodo_inicio  DATE          NOT NULL,
    periodo_fim     DATE          NOT NULL,
    valor_total     NUMERIC(15, 2) NOT NULL,
    tipo_chave_pix  VARCHAR(20),
    chave_pix       VARCHAR(140),
    status          VARCHAR(10)   NOT NULL DEFAULT 'PENDENTE'
);

CREATE INDEX ix_pagamentos_status ON pagamentos (status);
