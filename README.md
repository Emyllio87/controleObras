# Controle de Obras

Sistema web para controle de obras: funcionários, fornecedores, pedidos de orçamento,
autorização de pagamento, almoxarifado, diárias e pagamentos.

## Estrutura

- `backend/` — API REST em Spring Boot, PostgreSQL, JWT e geração de PDF com OpenPDF.
- `frontend/` — aplicação React com Vite, React Router, Axios e Tailwind CSS.
- `docker-compose.yml` — banco, back-end e front-end (Nginx).

## Subir o ambiente

1. Copie `.env.example` para `.env` e altere as senhas e o segredo do JWT.
2. Execute `docker compose up --build`.

Depois do primeiro start, o sistema cria o usuário administrador com o login e a
senha definidos em `ADMIN_LOGIN` e `ADMIN_SENHA`. Acesse `http://localhost` e faça
login com esse usuário. As demais telas e usuários são cadastrados na tela de
administração.

## Desenvolvimento

Back-end:

```
cd backend
mvn spring-boot:run
```

É necessário ter um PostgreSQL com o banco `controle_obras` criado. As variáveis
`DB_URL`, `DB_USER` e `DB_PASSWORD` podem ser definidas no ambiente.

Front-end:

```
cd frontend
npm install
npm run dev
```

O Vite usa a porta 5173 e encaminha as chamadas `/api` para `http://localhost:8080`.

## Banco de dados

O esquema é criado pelo Flyway a partir de `backend/src/main/resources/db/migration`.
Novas alterações devem entrar como um novo arquivo `V2__...sql`, sem editar uma
migração já aplicada.
