# Tech Case - Transactions routine

API REST para gerenciamento de contas e transações de cartão, desenvolvida em Java + Spring Boot.

## Tecnologias

- Java 21
- Spring Boot
- Spring Data JPA
- H2 Database (em memória)
- Gradle

## Arquitetura

Projeto estruturado em camadas:
- `repository` — entidades JPA e acesso a dados
- `service` — contratos (interfaces) das regras de negócio
- `useCase` — implementação das regras de negócio
- `web.controller` — endpoints REST
- `web.dto` — objetos de request/response
- `web.exception` — exceptions de domínio e tratamento global de erros

### Decisões de design
- **Normalização de sinal por tipo de operação**: o `OperationType` (enum) centraliza a regra de negócio que define se uma transação é positiva ou negativa, evitando que o client tenha controle sobre isso.
- **Exceptions de domínio + `@RestControllerAdvice`**: erros de negócio (conta não encontrada, tipo de operação inválido) são tratados de forma centralizada, retornando respostas HTTP padronizadas.

##  Como rodar

### Opção 1 — Docker 
\`\`\`bash
./run
\`\`\`

### Opção 3 — Local (sem Docker)
\`\`\`bash
./gradlew bootRun
\`\`\`

A aplicação sobe em `http://localhost:8080`.

## Rodando os testes

\`\`\`bash
./gradlew test
\`\`\`

## Endpoints

### Criar conta
\`POST /accounts\`
\`\`\`json
{
"document_number": "12345678900"
}
\`\`\`

### Consultar conta
\`GET /accounts/{accountId}\`

### Criar transação
\`POST /transactions\`
\`\`\`json
{
"account_id": 1,
"operation_type_id": 4,
"amount": 123.45
}
\`\`\`

## Banco de dados

H2 em memória. Console disponível em `http://localhost:8080/h2-console`.

## Tipos de transação

| ID | Tipo                  | Sinal    |
|----|------------------------|----------|
| 1  | Normal Purchase        | Negativo |
| 2  | Installment Purchase   | Negativo |
| 3  | Withdrawal              | Negativo |
| 4  | Credit Voucher          | Positivo |