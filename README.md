# Insurance Policy Management System

## Project overview

This is a REST API for managing insurance policies and claims. You can create and manage policies (health, auto, home, life), renew or cancel them, and submit and track claims with status updates (e.g. SUBMITTED, APPROVED, REJECTED). The API returns standardized success/error payloads and supports pagination for listing policies.

## Technologies used

- **Java 21**
- **Spring Boot 4** – Web, Validation, JPA, Data Redis, Kafka
- **PostgreSQL** – Primary database
- **Redis** - Cache-aside pattern
- **Kafka** - event broker
- **AWS SES** - email delivery

## Other Libraries
- **Liquibase** – Database migrations (schema + sample data on startup)
- **SpringDoc OpenAPI (Swagger)** – API documentation and Swagger UI
- **MapStruct** – DTO mapping
- **Lombok** – Boilerplate reduction
- **AWS SES SDK V2** - Email Sending 
- **Swagger/OpenAPI** - API documentation)
- **Spring Validation** - Annotations

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
| `AWS_SES_REGION`       | AWS region where SES is enabled                  | `eu-central-1`                                  |
| `AWS_SES_FROM_EMAIL`   | Verified SES sender email/domain address         | `no-reply@your-domain.com`                      |
| `AWS_ACCESS_KEY_ID`    | IAM access key ID (programmatic access)          | `AKIA...`                                       |
| `AWS_SECRET_ACCESS_KEY`| IAM secret key for the access key ID             | `...`                                           |

## AWS SES configuration steps

1. Log into AWS and choose the same region you set in `AWS_SES_REGION`.
2. Open Amazon SES and verify your sender identity:
   - verify an email address (quick start)
3. If your account is in the SES sandbox, verify recipient addresses too.
4. Request production access in SES when you need to email unverified recipients.
5. Create an IAM user (or role) with SES send permissions only.
6. Attach a policy such as:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ses:SendEmail",
        "ses:SendRawEmail"
      ],
      "Resource": "*"
    }
  ]
}
```

7. Create an access key for that IAM user and set:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
8. Set `AWS_SES_FROM_EMAIL` to a verified sender.

## SES sandbox limitations and testing

### Sandbox restrictions

New AWS accounts start in **SES sandbox mode** with these limitations:

- Can **only send to verified email addresses**
- Maximum 200 emails per day
- Maximum 1 email per second
- Cannot send to unverified recipients

Production access removes these restrictions.

### Verifying email addresses for testing

**Check sandbox status:**
1. Go to Amazon SES → **Account dashboard**
2. Look for "Your Amazon SES account is in the sandbox" message

**Verify sender email:**
1. Go to **Verified identities** → **Create identity**
2. Select **Email address** and enter your `AWS_SES_FROM_EMAIL`
3. Click the verification link sent to that address

**Verify recipient emails for testing:**
1. Go to **Verified identities** → **Create identity**
2. Select **Email address** and enter the test recipient (e.g., `yourtest@gmail.com`)
3. Recipient must click the verification link in their email
4. Repeat for each test email address

**Test:**
```bash
# Create a policy - triggers email to verified address
curl -X POST http://localhost:8080/api/v1/policies \
  -H "Content-Type: application/json" \
  -d '{
    "policyType": "HEALTH",
    "policyHolderEmail": "yourtest@gmail.com",
    "premiumAmount": 150.00,
    "coverageAmount": 50000.00
  }'
```

If the email isn't verified, you'll see: `MessageRejected: Email address is not verified`

### Moving to production

1. Go to **Account dashboard** → **Request production access**
2. Select "Transactional" mail type
3. Describe your use case (policy/claim notifications)
4. Submit (typically approved within 24 hours)

### Credentials options

- You can set `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` directly in your environment.
- If those are not provided, the application falls back to the AWS default credential chain (for example EC2/ECS/EKS role credentials, or your local AWS profile/session).

## Docker setup and run instructions

There are two ways to run this application: **with Docker** (recommended for production) or **locally** (for development).

### Option 1: Run everything with Docker (Recommended)

This option runs the entire stack (PostgreSQL, Redis, Kafka, and the Spring Boot app) in Docker containers.

**1. Ensure `.env` is present**

Copy the template and set your variables:
```bash
cp .env.example .env
```

Edit `.env` with your actual values for:
- Database credentials (`POSTGRES_USER`, `POSTGRES_PASSWORD`)
- Redis password (`REDIS_PASSWORD`)
- AWS SES credentials (if using email features)
- etc

**2. Build and start all services**

```bash
docker-compose up --build
```

Or run in detached mode (background):
```bash
docker-compose up --build -d
```

This starts:
- **PostgreSQL** on port `5432` (`insurance-postgres`)
- **Redis** on port `6379` (`insurance-redis`)
- **Kafka** on port `9092` (`insurance-kafka`)
- **Spring Boot App** on port `8080` (`insurance-app`)

**3. View logs (if running detached)**

```bash
docker-compose logs -f app
```

**4. Stop the stack**

```bash
docker-compose down
```

To remove volumes (deletes all data):
```bash
docker-compose down -v
```

**How the Dockerfile works:**

The `Dockerfile` uses a **multi-stage build** for optimal image size and security:

- **Stage 1 (Build)**: Uses `maven:3.9-eclipse-temurin-21-alpine` to compile the application
  - Copies `pom.xml` and downloads dependencies (cached layer)
  - Copies source code and builds the JAR with `mvn clean package -DskipTests`
  
- **Stage 2 (Runtime)**: Uses `eclipse-temurin:21-jre-alpine` (JRE only, smaller image)
  - Creates non-root user `spring` for security
  - Copies only the compiled JAR from build stage
  - Exposes port `8080`
  - Configures JVM container support and memory limits

**Environment configuration in Docker:**

The `docker-compose.yml` file automatically configures the application to connect to containerized services:
- Database URL: `jdbc:postgresql://postgres:5432/insurance_db`
- Redis host: `redis`
- Kafka bootstrap servers: `kafka:9092`
- AWS credentials from `.env` file

### Option 2: Run dependencies in Docker, app locally

This option runs only PostgreSQL, Redis, and Kafka in Docker, while running the Spring Boot app on your local machine (useful for development).

**1. Start only the dependencies**

```bash
docker-compose up -d postgres redis kafka
```

**2. Ensure `.env` is present**

```bash
cp .env.example .env
```

Set variables for **localhost** connections:
- `POSTGRES_URL=jdbc:postgresql://localhost:5432/insurance_db`
- `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`
- `REDIS_HOST=localhost`
- etc

**3. Load environment variables**

- **Unix/Linux/macOS:**
  ```bash
  set -a && source .env && set +a
  ```

- **Windows PowerShell:**
  ```powershell
  Get-Content .env | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
      [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2])
    }
  }
  ```

**4. Run the Spring Boot application**

```bash
./mvnw spring-boot:run
```

On Windows:
```bash
mvnw.cmd spring-boot:run
```

**5. Stop the dependencies**

```bash
docker-compose down
```

The API is available at **http://localhost:8080** with either option.

## Running tests

The project includes comprehensive unit and integration tests.

**Run all tests:**

```bash
./mvnw test
```

On Windows:
```bash
mvnw.cmd test
```

**Test coverage includes:**

- **Unit tests** for service layer (`EmailServiceImpl`, `PolicyServiceImpl`, `ClaimServiceImpl`)
- **Custom validator tests** (`@ValidDateRange`, `@PremiumAmount`, `@Email`, `@CustomerFullName`)
- **Integration tests** for REST endpoints (`PolicyController`)


## API documentation (Swagger)

When the application is running:

- **Swagger UI (interactive docs):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Use Swagger UI to explore endpoints, try requests, and see request/response examples and error formats.
