# Tech Case - Transactions API

A REST API for managing cardholder accounts and transactions, built with Java + Spring Boot.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- H2 Database (in-memory)
- Gradle
- Lombok

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
- **Structured logging**: key business events (account not found) are logged for traceability, without exposing sensitive data.

## How to run

### Option 1 — Docker 
```bash
./run
```

### Option 2 — Local 
```bash
./gradlew bootRun
```

The application starts on `http://localhost:8080`.

## Running the tests

```bash
./gradlew test
```

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

## Database

H2 in-memory database. Console available (if enabled) at `http://localhost:8080/h2-console`.

## Operation types

| ID | Type                  | Sign     |
|----|------------------------|----------|
| 1  | Normal Purchase        | Negative |
| 2  | Installment Purchase   | Negative |
| 3  | Withdrawal              | Negative |
| 4  | Credit Voucher          | Positive |