# Saving Planner

Spring Boot + Kotlin backend for user registration and personal finance planning.

## Current project setup

This README reflects the current codebase configuration:

- Kotlin `2.2.21`
- Spring Boot `3.3.5`
- Java toolchain `21`
- PostgreSQL as the runtime database
- Spring Security with JWT Bearer authentication
- CORS enabled for local frontend development
- Docker support for running the app with PostgreSQL
- Testcontainers-based PostgreSQL integration tests
- Sensitive runtime credentials sourced from environment variables

## Runtime defaults

### Application ports

- Local app run: `http://localhost:8080`
- Docker Compose app run: `http://localhost:8081`

### Database ports

- Local PostgreSQL expected by default: `localhost:5433`
- Docker PostgreSQL exposed to the host: `localhost:5434`

### Sensitive runtime configuration

The application no longer commits sensitive login values into source control.

These values must come from environment variables at runtime:

- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

Optional bootstrap-admin credentials should also come from environment variables if you enable that feature:

- `BOOTSTRAP_ADMIN_ENABLED`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`

### Default CORS origin

Browser requests are allowed from:

- `http://localhost:3000`

You can override this with the `CORS_ALLOWED_ORIGINS` environment variable.

## Database configuration

The application is configured to use PostgreSQL:

- JDBC URL: `jdbc:postgresql://${DB_HOST:<hostname>}:${DB_PORT:<port>}/${DB_NAME:<dbname>}`
- username: `${DB_USERNAME:<username>}`
- password: `${DB_PASSWORD:<password>}`

Hibernate SQL logging is off by default in `src/main/resources/application.properties`.

## Run locally without Docker

Make sure PostgreSQL is running locally and accessible on port `5433`, or override the connection with environment variables.

### Option 1: export the required variables in the current terminal session

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5433"
$env:DB_NAME="saving_planner"
$env:DB_USERNAME="<dbuser>"
$env:DB_PASSWORD="<dbpassword>"
$env:JWT_SECRET="<minimum-32-character-secret>"
.\gradlew.bat bootRun
```

### Option 2: enable the bootstrap admin locally

Only set these if you want the app to create an initial admin user on startup:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED="true"
$env:BOOTSTRAP_ADMIN_EMAIL="admin@example.com"
$env:BOOTSTRAP_ADMIN_PASSWORD="<strong-password>"
```

Then start the app in the same terminal:

```powershell
.\gradlew.bat bootRun
```

### Build the runnable jar

```powershell
.\gradlew.bat bootJar
```

The jar is created under `build/libs/`.

## Run with Docker Compose

This project includes a `Dockerfile` and `docker-compose.yml` for running the Spring Boot app together with PostgreSQL.

### Start the full stack

```powershell
docker compose up --build
```

### Start in the background

```powershell
docker compose up --build -d
```

### Stop the stack

```powershell
docker compose down
```

### Stop the stack and remove database data

```powershell
docker compose down -v
```

### View logs

```powershell
docker compose logs -f
```

Only the app logs:

```powershell
docker compose logs -f app
```

Only the database logs:

```powershell
docker compose logs -f postgres
```

## Docker `.env` configuration

`docker-compose.yml` expects a local `.env` file with these variables.

Start by copying the committed template:

```powershell
Copy-Item .env.example .env
```

Then fill in your real secrets in `.env`.

The main values are:

- `PSQL_DB_HOST_NAME`
- `PSQL_DB_PORT`
- `PSQL_DB_NAME`
- `PSQL_DB_USER`
- `PSQL_DB_PASSWORD`
- `JWT_SECRET`

Optional bootstrap admin variables:

- `BOOTSTRAP_ADMIN_ENABLED`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`

The app container maps them to:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`

- inside the Compose network, the app should connect to the `postgres` service on port `5432`
- from your host machine, PostgreSQL is exposed on `localhost:5434`
- the app container is exposed on `localhost:8081`

The Docker image is built with:

```text
./gradlew bootJar --no-daemon -x test
```

so image builds skip tests during the Docker build stage.

## Authentication and API usage

The application now uses JWT Bearer tokens.

### Login to get a token

```powershell
$loginBody = @{
  email = "admin@example.com"
  password = "<password>"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/v1/auth/login" `
  -ContentType "application/json" `
  -Body $loginBody
```

### Call a protected endpoint

```powershell
$token = $loginResponse.accessToken

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users" -Headers @{ Authorization = "Bearer $token" }
```

If you run the backend in Docker, use port `8081` instead of `8080`.

## Frontend integration notes

The backend is currently configured for browser calls from `http://localhost:3000`.

To allow more than one frontend origin before starting the backend:

```powershell
$env:CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:5173"
```

Because the API uses Bearer tokens, frontend requests must first call the login endpoint and then send the `Authorization` header. Cookie-based login is not configured.

Example frontend request for user creation:

```javascript
const loginResponse = await fetch('<localhost:url>/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'admin@example.com',
    password: '<password>'
  })
});

const { accessToken } = await loginResponse.json();

const response = await fetch('<localhost:url>/api/v1/users/user', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${accessToken}`
  },
  body: JSON.stringify({
    companyId: crypto.randomUUID(),
    email: 'frontend@example.com',
    firstName: 'Frontend',
    lastName: 'User',
    password: 'secret123',
    role: 'USER',
    telephoneNumber: '+1 1234567890',
    onboardingDone: true
  })
});
```

Notes:

- `CreateUserRequest` accepts `password` and alias `passwordHash`
- `CreateUserRequest` accepts `telephoneNumber` and alias `phoneNumber`
- telephone numbers are normalized to digits before persistence

## Main endpoints

### User endpoints

Base path: `/api/v1/users`

- `POST /api/v1/users/user`
- `GET /api/v1/users`
- `GET /api/v1/users/{userId}`
- `GET /api/v1/users/allinfo/{userId}`
- `PUT /api/v1/users/{userId}`
- `DELETE /api/v1/users/{userId}`

### Personal finance endpoints

Base path: `/api/v1/finance/overview`

- `POST /api/v1/finance/overview/create`
- `GET /api/v1/finance/overview`
- `GET /api/v1/finance/overview/{financeId}`
- `PUT /api/v1/finance/overview/{financeId}`
- `DELETE /api/v1/finance/overview/{financeId}`

The finance read endpoints now return DTO responses instead of exposing JPA entities directly. This avoids Hibernate lazy-proxy serialization issues for nested `monthlyExpenses`, `insurances`, and `subscriptions`.

## Example personal finance create request

`POST /api/v1/finance/overview/create`

```json
{
  "startDate": "2026-03-01T00:00:00Z",
  "endDate": "2026-03-31T00:00:00Z",
  "monthlyIncome": 30000.0,
  "monthlyExpenses": {
    "mortgagePayment": 5000.0,
    "sharedHouseCost": 1000.0,
    "foodBudget": 2500.0,
    "carLoan": 1000.0,
    "creditCardBill": 2500.0,
    "electricityBill": 1000.0,
    "studentLoans": 2000.0,
    "tollFees": 500.0,
    "insurances": [
      {
        "insuranceType": "health insurance",
        "insuranceCost": 1000.0,
        "insuranceCompany": "CVS Health"
      }
    ],
    "subscriptions": [
      {
        "subscriptionName": "Netflix",
        "subscriptionCost": 100.0
      }
    ]
  },
  "consumption": 2500.0,
  "savings": 5000.0,
  "investments": 4000.0
}
```

Notes:

- `insurances` must be a JSON array
- `subscriptions` must be a JSON array
- use `subscriptionName`, not `subscriptionType`
- successful create/update finance endpoints currently return plain success strings rather than JSON objects

## Build and test

Run all tests:

```powershell
.\gradlew.bat test
```

Run only the finance service tests:

```powershell
.\gradlew.bat test --tests "com.finance.saving_planner.service.impl.PersonalFinanceServiceImplTest"
```

The test suite includes PostgreSQL integration tests backed by Testcontainers. Docker must be running for those tests to pass.

## Troubleshooting

### Application fails to start because a required placeholder cannot be resolved

Make sure you have exported all required secret environment variables before starting the app:

- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

### Application fails with datasource configuration errors

Make sure PostgreSQL is running and these values are correct for your environment:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`

### Docker app cannot connect to PostgreSQL

Inside Docker Compose, the app should connect to the configured PostgreSQL service name, typically `postgres`, on container port `5432`.

### Database insert fails after entity refactors

If you refactor entities such as `PersonalFinance` and `MonthlyExpenses`, an older local PostgreSQL schema can keep stale columns or `NOT NULL` constraints. In that case, reset the local schema or drop the outdated tables before restarting the app.

### Browser request blocked by CORS

Make sure the frontend origin is included in `CORS_ALLOWED_ORIGINS`, or use the default `http://localhost:3000`.

### Port 8080 already in use

Find the process using the port and stop it, or start the app on another port:

```powershell
netstat -ano | findstr :8080
.\gradlew.bat bootRun --args="--server.port=8081"
```

## Qodana code quality checks

This repository includes a GitHub Actions workflow at `.github/workflows/qodana_code_quality.yml` and a Qodana config in `qodana.yaml`.

The configured linter is:

```yaml
linter: jetbrains/qodana-jvm:2025.3
```

This release linter requires a Qodana Cloud access token.

If the GitHub repository secret `QODANA_TOKEN` is missing, the workflow skips the Qodana scan with a clear message instead of failing the pipeline.

To enable Qodana in GitHub Actions:

1. Create a token at `https://qodana.cloud`
2. Open GitHub → Settings → Secrets and variables → Actions
3. Add a repository secret named `QODANA_TOKEN`

## Postman note

There is a `postman/README.md` file with additional Postman notes. If you later add a Postman collection or environment export, place those files in the `postman/` folder.
