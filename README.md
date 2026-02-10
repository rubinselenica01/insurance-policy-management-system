# Insurance Policy Management System

## Project overview

This is a REST API for managing insurance policies and claims. You can create and manage policies (health, auto, home, life), renew or cancel them, and submit and track claims with status updates (e.g. SUBMITTED, APPROVED, REJECTED). The API returns standardized success/error payloads and supports pagination for listing policies.

## Technologies used

- **Java 21**
- **Spring Boot 4** – Web, Validation, JPA, Data Redis, Kafka
- **PostgreSQL** – Primary database
- **SpringDoc OpenAPI (Swagger)** – API documentation and Swagger UI
- **MapStruct** – DTO mapping
- **Lombok** – Boilerplate reduction

## Prerequisites

- **Java 17+** (project is built with Java 21)
- **Docker** (for running PostgreSQL and optional services)
- **Maven** 

## Environment variables and configuration guide

The application is configured via environment variables. A real `.env` file is **not** committed to the repository for security. Use the provided template instead.

1. **Copy the example file**  
   Create your local environment file from the template:
   ```bash
   cp .env.example .env
   ```
2. **Edit `.env`**  
   Set the values for your environment (see [Environment variables needed](#environment-variables-needed) below).
3. **Load variables when running**  
   - **IDE:** Configure your run configuration to load from `.env` or set the variables in the environment.
   - **Terminal:** Export the variables in your shell or use a tool that loads `.env` (e.g. `set -a && source .env && set +a` on Unix; on Windows PowerShell you can read `.env` and set `$env:VAR = "value"` per variable).

Never commit `.env`; only `.env.example` (without secrets) should be in version control.

## Environment variables needed

| Variable               | Description                                      | Example (local)                                 |
|------------------------|--------------------------------------------------|-------------------------------------------------|
| `POSTGRES_URL`         | JDBC URL for PostgreSQL                          | `jdbc:postgresql://localhost:5432/insurance_db` |
| `POSTGRES_USER`        | Database username                                | `your_db_user`                                  |
| `POSTGRES_PASSWORD`    | Database password                                | `your_db_password`                              |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap address                       | `localhost:9094`                                |
| `REDIS_HOST`           | Redis host                                       | `localhost`                                     |
| `REDIS_PORT`           | Redis port                                       | `6379`                                          |
| `REDIS_PASSWORD`       | Redis password (required; compose enforces it)   | `your_redis_password`                           |

## Docker setup and run instructions

### Start the stack (Postgres, Redis, Kafka)

1) Ensure `.env` is present (copied from `.env.example`) and set the vars as you wish

2) Bring up the services:

```bash
docker compose up -d
```

This starts:
- **PostgreSQL** on `5432` (`insurance-postgres`)
- **Redis** on `6379` with `--requirepass` from `REDIS_PASSWORD` (`insurance-redis`)
- **Kafka** on `9094` (PLAINTEXT_HOST) (`insurance-kafka`)

To stop:
```bash
docker compose down
```

### Run the application

1. Ensure the stack is running (Postgres/Redis/Kafka via `docker compose up -d`) and `.env` is configured.
2. Load the environment variables (see [Environment variables and configuration guide](#environment-variables-and-configuration-guide)).
3. Start the Spring Boot app:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows:

   ```bash
   mvnw.cmd spring-boot:run
   ```

The API is available at **http://localhost:8080** (default Spring Boot port).

## API documentation (Swagger)

When the application is running:

- **Swagger UI (interactive docs):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Use Swagger UI to explore endpoints, try requests, and see request/response examples and error formats.
