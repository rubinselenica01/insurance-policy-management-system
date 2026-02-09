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

| Variable           | Description                                      | Example (local)                              |
|--------------------|--------------------------------------------------|----------------------------------------------|
| `POSTGRES_URL`     | JDBC URL for the PostgreSQL database             | `jdbc:postgresql://localhost:5432/insurance_db` |
| `POSTGRES_USER`    | Database username                                | `your_db_user`                               |
| `POSTGRES_PASSWORD`| Database password                                | `your_db_password`                           |

These are the variables referenced in `application.yaml`.

## Docker setup and run instructions

### Start PostgreSQL only

To run the database with Docker (e.g. for local development):

```bash
docker-compose up -d
```

This starts a PostgreSQL 15 container:

- **Container name:** `insurance-postgres`
- **Port:** `5432`
- **Database:** `insurance_db`
- **Credentials:** Set in `docker-compose.yml` (e.g. user/password). Ensure your `.env` matches the URL and credentials you use to connect from the app (e.g. `POSTGRES_URL=jdbc:postgresql://localhost:5432/insurance_db`, `POSTGRES_USER`, `POSTGRES_PASSWORD`).

To stop:

```bash
docker-compose down
```

### Run the application

1. Ensure PostgreSQL is running (e.g. via `docker-compose up -d`) and `.env` is configured.
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
