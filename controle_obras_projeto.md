# Sistema de Controle de Obras — Escopo do Projeto

## 1. Visão geral

Sistema web para controle de obras, com autenticação por login, perfis de acesso por usuário e módulos de cadastro, pedidos de orçamento, autorização de pagamento, almoxarifado e controle de diárias de funcionários.

Todas as telas são CRUD (criar, listar, editar, excluir) e possuem um botão **Limpar** para zerar os campos do formulário.

---

## 2. Perfis e controle de acesso

- Login com usuário e senha.
- Cada usuário possui um perfil de acesso que define quais telas ele pode acessar.
- Telas sem permissão não aparecem no menu e não podem ser acessadas diretamente pela URL.
- Senhas armazenadas com hash BCrypt.
- Autenticação por token JWT, com expiração configurável.

**Tela de Administrador** (restrita ao perfil administrador)

- Cadastrar, editar e excluir usuários.
- Definir, para cada usuário, quais telas ele acessa (funcionários, fornecedores, pedidos, autorização de pagamento, almoxarifado, diárias, pagamentos).

---

## 3. Módulos e telas

### 3.1 Usuários (administrador)

Campos: nome, login, senha, status (ativo/inativo) e telas permitidas.

### 3.2 Funcionários

| Campo | Observação |
|---|---|
| Nome | obrigatório |
| Telefone | máscara conforme seção 4.1 |
| Função | ex.: pedreiro, servente, eletricista |
| Valor da diária | valor monetário |
| Tipo de chave PIX | filtro: CPF, CNPJ, telefone, e-mail ou chave aleatória |
| Chave PIX | máscara conforme o tipo selecionado |
| CPF | máscara e validação conforme seção 4.1 |

A listagem permite filtrar por tipo de chave PIX.

### 3.3 Fornecedores

| Campo | Observação |
|---|---|
| Razão social | obrigatório |
| CNPJ | máscara e validação conforme seção 4.1 |
| Telefone | máscara conforme seção 4.1 |
| Nome do contato | |
| Tipo de chave PIX | filtro (mesmas opções de funcionários) |
| Chave PIX | máscara conforme o tipo selecionado |

### 3.4 Pedidos de orçamento

- Número sequencial do pedido, gerado automaticamente (ex.: 000001).
- Campo **Descrição** no cabeçalho do pedido, além da descrição de cada item.
- Ao selecionar um fornecedor cadastrado, seus dados (razão social, CNPJ, telefone, contato e chave PIX) são carregados automaticamente.
- Inclusão de itens do pedido. Cada item adicionado aparece em uma lista abaixo do formulário com:
  - número do item (sequencial, gerado automaticamente)
  - descrição
  - quantidade
  - unidade
- Ações: adicionar item, remover item e salvar pedido.
- Botão **Gerar PDF** do pedido, com cabeçalho contendo os dados do fornecedor e a tabela de itens.

### 3.5 Autorização de pagamento

- Vincula um pedido de orçamento existente. Cada pedido gera uma única autorização de pagamento.
- Número sequencial da autorização, gerado automaticamente.
- Exibe os dados do pedido (fornecedor e itens) e o campo **Valor**.
- Campo para anexar imagem (JPG, PNG) e/ou PDF, com limite de tamanho definido.
- Ao salvar, o documento é criado e listado em uma tabela abaixo da tela.
- Botão **Gerar PDF** da autorização, disponível também na listagem.

### 3.6 Almoxarifado (ferramentas)

- Cadastro de ferramentas com nome/descrição, quantidade e unidade.
- Listagem das ferramentas cadastradas abaixo do formulário.
- Botão **Gerar PDF** com a relação de ferramentas.

### 3.7 Diárias

- Cada funcionário cadastrado tem um card separado na tela.
- Dentro do card, as diárias são lançadas dia a dia, com data selecionada pelo calendário.
- Cada diária tem valor próprio, calculado a partir do valor da diária informado no cadastro do funcionário.
- O valor pode ser alterado em um lançamento específico, sem alterar o cadastro do funcionário.
- O valor total das diárias lançadas é somado automaticamente no card.
- Botão **Fechar diárias**: soma todas as diárias lançadas no card, envia o fechamento para a tela de Pagamentos e zera o card para novos lançamentos.
- Cada fechamento gera um registro no histórico de diárias.

### 3.8 Pagamentos

- Recebe os fechamentos de diárias enviados pelo módulo de Diárias.
- Lista os pagamentos com funcionário, período, valor total e chave PIX.
- Cada pagamento tem status: **pendente** ou **pago**. A mudança de status é feita nesta tela.
- Permite filtrar por status.

### 3.9 Histórico de diárias

- Lista os fechamentos de diárias, com funcionário, período, diárias de cada fechamento e valor total.
- Somente consulta: fechamentos já realizados não são alterados.

---

## 4. Regras de negócio

1. Usuário sem permissão para uma tela não a visualiza nem acessa pela URL.
2. Pedido de orçamento só pode ser criado com fornecedor cadastrado.
3. Itens do pedido são numerados automaticamente na ordem de inclusão.
4. Autorização de pagamento só é criada a partir de um pedido de orçamento existente.
5. Diárias fechadas não podem ser alteradas; o fechamento gera um registro na tela de Pagamentos.
6. Chaves PIX respeitam o tipo selecionado (ex.: CPF com 11 dígitos, CNPJ com 14).
7. CPF e CNPJ são validados quanto ao formato, aos dígitos verificadores e à unicidade.
8. Cada pedido de orçamento gera uma única autorização de pagamento.
9. Pedidos de orçamento e autorizações de pagamento recebem número sequencial gerado automaticamente.
10. Cada diária lançada recebe o valor da diária informado no cadastro do funcionário, que pode ser alterado no lançamento.
11. Ao fechar as diárias, o card do funcionário é zerado e o fechamento passa a constar no histórico de diárias.

### 4.1 Máscaras, validações e formatação

Todos os campos com máscara ou formato definido são validados no front-end em tempo real e revalidados no back-end antes de salvar.

**Máscaras de entrada**

| Campo | Máscara | Validação |
|---|---|---|
| CPF | `000.000.000-00` | 11 dígitos e dígitos verificadores válidos |
| CNPJ | `00.000.000/0000-00` | 14 dígitos e dígitos verificadores válidos |
| Telefone | `(00) 0000-0000` ou `(00) 00000-0000` | 10 ou 11 dígitos com DDD válido |
| Chave PIX — CPF | igual à máscara de CPF | validação de CPF |
| Chave PIX — CNPJ | igual à máscara de CNPJ | validação de CNPJ |
| Chave PIX — telefone | `+55 (00) 00000-0000` | 11 dígitos após o código do país |
| Chave PIX — e-mail | sem máscara | formato de e-mail válido |
| Chave PIX — aleatória | sem máscara | formato UUID (32 caracteres hexadecimais com hífens) |

- A máscara da chave PIX muda conforme o tipo selecionado.
- A máscara é removida ao salvar. O banco armazena somente os dígitos, exceto e-mail e chave aleatória.

**Valores monetários** (valor da diária, valor do pedido, valor da autorização e total de diárias)

- Exibição no formato `R$ 1.234,56`, com ponto como separador de milhar, vírgula como decimal e duas casas.
- A entrada aceita apenas números e a formatação ocorre durante a digitação.
- Valor mínimo de 0,01. Valores negativos não são permitidos.
- Armazenamento em `NUMERIC(15,2)` no PostgreSQL, nunca em ponto flutuante.
- Listagens e PDFs seguem o mesmo formato.

**Quantidades**

- Quantidade de itens e de ferramentas: inteiros positivos. Quando a unidade exigir, aceita decimais com até 3 casas (ex.: metros, quilos).
- Número do item do pedido: gerado automaticamente, sem edição manual.

**Datas**

- Seleção obrigatória por calendário (componente de data), sem digitação manual.
- Exibição no formato `dd/mm/aaaa`.
- Lançamento de diária não pode ser feito para data futura.
- Datas de emissão de pedidos, autorizações e lançamentos de diárias seguem o mesmo componente.

**Textos**

- Espaços no início e no fim dos campos são removidos automaticamente.
- Campos obrigatórios são sinalizados e impedem o envio quando vazios.

**Mensagens de erro**

- Cada campo inválido exibe mensagem específica abaixo dele (ex.: "CPF inválido", "CNPJ deve conter 14 dígitos").

---

## 5. Relatórios PDF

Os PDFs são gerados no back-end com OpenPDF.

| Documento | Conteúdo |
|---|---|
| Pedido de orçamento | Dados do fornecedor, número sequencial, data, descrição do pedido, tabela de itens (número, descrição, quantidade, unidade) |
| Autorização de pagamento | Dados do pedido, fornecedor, valor, referência ao anexo |
| Almoxarifado | Relação de ferramentas com quantidade e unidade |
| Fechamento de diárias | Funcionário, diárias lançadas e total |

---

## 6. Arquitetura

### 6.1 Back-end em camadas

```
Controller  →  Service  →  Repository  →  PostgreSQL
    ↑
    DTOs (entrada e saída)
```

- **Controller:** recebe as requisições REST, valida a entrada e devolve DTOs.
- **Service:** contém as regras de negócio e as validações de CPF, CNPJ e PIX.
- **Repository:** interfaces Spring Data JPA para acesso ao banco.
- **Entidades JPA:** mapeamento das tabelas, sem exposição direta nas respostas da API.
- **Segurança:** Spring Security com filtro JWT, `@PreAuthorize` nos endpoints por tela.
- **PDF:** serviço dedicado que recebe dados já validados e gera os arquivos com OpenPDF.
- **Arquivos:** serviço dedicado para upload, com validação de tipo e tamanho.

### 6.2 Front-end

```
src/
  pages/        telas completas (uma por módulo)
  components/   componentes reutilizáveis (botões, campos com máscara, tabelas, cards)
  services/     chamadas à API com Axios
  routes/       rotas com React Router e proteção por permissão
  hooks/        lógica compartilhada (ex.: controle de sessão)
```

- Layout com `Outlet` para as telas internas, com menu exibido conforme as permissões do usuário.
- Estilização com Tailwind CSS.

---

## 7. Stack

| Camada | Tecnologia |
|---|---|
| Back-end | Spring Boot, Spring Data JPA, Spring Security + JWT |
| Banco de dados | PostgreSQL |
| Front-end | React, React Router, Axios, Tailwind CSS |
| PDF | OpenPDF (back-end) |
| Infraestrutura | Docker e Docker Compose |

---

## 8. Execução com Docker

O sistema roda inteiramente em containers, com três serviços orquestrados por Docker Compose:

| Serviço | Imagem / base | Porta |
|---|---|---|
| `db` | `postgres` (versão fixada) | 5432 (interna) |
| `backend` | build multi-stage com JDK e JRE | 8080 |
| `frontend` | build com Node e servido por Nginx | 80 |

- Variáveis de ambiente para credenciais do banco, segredo do JWT e tempo de expiração, sem valores fixos no código.
- Volume nomeado para os dados do PostgreSQL e outro para os arquivos anexados.
- `healthcheck` no banco, e o back-end só inicia após o banco estar pronto.
- Nginx encaminha as requisições `/api` para o back-end.
- Um único comando sobe todo o ambiente.

---

## 9. Requisitos não funcionais

- Interface responsiva em português do Brasil.
- Upload aceito: imagem (JPG, PNG) e PDF, com limite de tamanho definido.
- Validação de formulários no front-end e no back-end.
- Mensagens de sucesso e erro em todas as operações.
- Migrações de banco versionadas, para que o esquema seja reproduzível em qualquer ambiente.

---

## 10. Pendências de definição

Nenhuma pendência em aberto no momento.

---

## 11. Entregáveis

1. Sistema funcional com login e controle de perfis.
2. Todas as telas em CRUD com botão Limpar.
3. Geração de PDF nos quatro documentos da seção 5.
4. Ambiente completo executável com Docker Compose.
5. Este documento atualizado conforme as pendências forem resolvidas.
