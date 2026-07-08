# Tech Case - Transactions API

A REST API for managing cardholder accounts and transactions, built with Java + Spring Boot.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL (prod) / H2 (dev)
- Flyway (database migrations)
- Testcontainers (integration tests)
- Docker & Docker Compose
- Gradle
- Lombok
- Springdoc OpenAPI (Swagger)
- JaCoCo (test coverage)

## Architecture

The project follows a layered structure:
- `repository` — JPA entities and data access
- `service` — business logic contracts (interfaces)
- `useCase` — business logic implementation
- `web.controller` — REST endpoints
- `web.dto` — request/response objects
- `web.exception` — domain exceptions and global error handling

### Design decisions
- **Amount sign normalization by operation type**: the `OperationType` enum centralizes the business rule that defines whether a transaction is positive or negative, so the client cannot control this directly — it's enforced by the domain regardless of the sign sent in the request.
- **Domain exceptions + `@RestControllerAdvice`**: business errors (account not found, invalid operation type) are handled centrally, returning standardized HTTP responses instead of leaking stack traces.
- **`@ManyToOne` relationship between Transaction and Account**: transactions hold a real reference to their account (not just a raw ID), enforcing referential integrity at the database level via a foreign key constraint. The relationship is `LAZY` on purpose, to avoid loading the full account whenever a transaction is fetched.
- **Payment discharge**: when a `CREDIT_VOUCHER` transaction is created, the system automatically discharges outstanding negative balances from previous purchase/withdrawal transactions, oldest first (FIFO), either totally or partially. Any remaining credit after all debts are settled stays as a positive balance on the payment transaction itself.
- **Database migrations with Flyway**: schema changes are version-controlled through SQL migration scripts instead of relying on Hibernate's automatic DDL generation in production (`ddl-auto: validate`).
- **Structured logging**: key business events (account not found) are logged for traceability, without exposing sensitive data.

## Profiles

- **dev** (default): runs with H2 in-memory database, no external dependencies needed.
- **prod**: runs with PostgreSQL, started automatically via Docker Compose. Schema is managed by Flyway migrations.

## ▶️ How to run

### Option 1 — Docker Compose (recommended, runs app + PostgreSQL)
```bash
./run
```

### Option 2 — Docker Compose manually
```bash
docker compose up --build
```

### Option 3 — Local with dev profile (H2, no Docker needed)
```bash
./gradlew bootRun
```

The application starts on `http://localhost:8080`.

### Resetting the database
To start with a completely clean database (removes the Docker volume):
```bash
./reset
```

##  Running the tests

```bash
./gradlew test
```

This runs unit tests, controller slice tests (`@WebMvcTest`), and integration tests (`@SpringBootTest` + Testcontainers, using a real PostgreSQL container). Docker must be running for the integration tests to execute.

### Test coverage

Test coverage is measured with JaCoCo, with a minimum threshold of 90%.
```bash
./gradlew jacocoTestReport
```
Report available at `build/reports/jacoco/test/html/index.html`.

### Test structure
src/test/java/com/waleria/techcase/
├── useCase/           (unit tests — services, discharge logic, operation type rules)
├── web/
│   ├── controller/     (slice tests — @WebMvcTest)
│   └── exception/      (unit tests — GlobalExceptionHandler)
└── integration/        (integration tests — Testcontainers + real PostgreSQL)
├── AccountIntegrationTest.java
├── TransactionIntegrationTest.java
└── PaymentDischargeIntegrationTest.java

## API Documentation

Interactive API documentation (Swagger UI) is available once the application is running:

http://localhost:8080/swagger-ui.html

## Endpoints

### Create account
`POST /accounts`
```json
{
  "document_number": "12345678900"
}
```

### Retrieve account
`GET /accounts/{accountId}`

### Create transaction
`POST /transactions`
```json
{
  "account_id": 1,
  "operation_type_id": 4,
  "amount": 123.45
}
```

### List transactions by account
`GET /transactions?account_id={accountId}`

Returns all transactions for the given account, ordered by event date, including the current balance of each one.

## Database

- **dev**: H2 in-memory. Console available at `http://localhost:8080/h2-console`.
- **prod**: PostgreSQL, schema managed via Flyway migrations in `src/main/resources/db/migration`.

## Operation types

| ID | Type                  | Sign     | Discharges previous debts |
|----|------------------------|----------|----------------------------|
| 1  | Normal Purchase        | Negative | No                          |
| 2  | Installment Purchase   | Negative | No                          |
| 3  | Withdrawal              | Negative | No                          |
| 4  | Credit Voucher          | Positive | Yes                         |

## Payment discharge

When a `Credit Voucher` (payment) transaction is created, the system discharges the paid amount against outstanding negative balances from previous purchase/withdrawal transactions, oldest first:
- If the payment fully covers a debt, that transaction's balance becomes `0`.
- If the payment partially covers a debt, the remaining balance stays negative.
- If the payment exceeds all outstanding debts, the remaining amount is kept as a positive balance on the payment transaction itself.