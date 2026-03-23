# Saving Planner

Spring Boot + Kotlin API for user registration and personal finance planning.

## Current project setup

This README reflects the current codebase configuration:

- Kotlin `2.2.21`
- Spring Boot `3.3.5`
- Java toolchain `21`
- PostgreSQL as the runtime database
- Spring Security with **HTTP Basic authentication**
- Docker support for running the app together with PostgreSQL
- Testcontainers-based PostgreSQL integration tests

## Runtime defaults verified from the project

### Application ports

- **Local app run:** `http://localhost:8080`
- **Docker app run:** `http://localhost:8081`

### Database ports

- **Local PostgreSQL expected by default:** `localhost:5433`
- **Docker PostgreSQL exposed to host:** `localhost:5434`

### Default HTTP Basic credentials

The current `SecurityConfig` defines one in-memory user:

- username: `<username>`
- password: `<password>`

> This is the credential for calling the secured API right now. Because credentials are explicitly configured in `SecurityConfig`, Spring Boot will **not** print an auto-generated password in the logs.

### Default CORS setup

Browser requests are currently allowed from:

- `http://localhost:3000`

## Database configuration

The application is configured to use PostgreSQL:

- JDBC URL: `jdbc:postgresql://${DB_HOST:<hostname>}:${DB_PORT:<port>}/${DB_NAME:<dbname>}`
- username: `${DB_USERNAME:<username>}`
- password: `${DB_PASSWORD:<password>}`

Hibernate SQL logs are **off by default** in `application.properties`.

If you want extra SQL logging during development, use the local profile:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

## Run locally without Docker

Make sure PostgreSQL is running locally and accessible on port `5433`, or provide your own values with environment variables.

### Option 1: use the defaults

This works if your local database matches:

- host: `<hostname>`
- port: `<port>`
- database: `<dbname>`
- username: `<username>`
- password: `<password>`

Then run:

```powershell
.\gradlew.bat bootRun
```

### Option 2: override database settings for the current terminal session

```powershell
$env:DB_HOST="<dbhost>"
$env:DB_PORT="<dbport>"
$env:DB_NAME="<dbname>"
$env:DB_USERNAME="<dbuser>"
$env:DB_PASSWORD="<dbpassword>"
.\gradlew.bat bootRun
```

## Run with Docker Compose

This project includes:

- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`

The Compose stack starts:

- the Spring Boot app in one container
- PostgreSQL in another container

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

App logs only:

```powershell
docker compose logs -f app
```

PostgreSQL logs only:

```powershell
docker compose logs -f postgres
```

## Docker environment variables

`docker-compose.yml` expects a `.env` file for PostgreSQL and app configuration.

The current Compose file uses these variable names:

- `PSQL_DB_HOST_NAME`
- `PSQL_DB_PORT`
- `PSQL_DB_NAME`
- `PSQL_DB_USER`
- `PSQL_DB_PASSWORD`

The app container maps them to:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`

## Test the API

All endpoints currently require Basic Auth.

### PowerShell example

```powershell
$pair = 'username:password'
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$token = [Convert]::ToBase64String($bytes)
Invoke-WebRequest -Uri "http://localhost:8081/api/v1/users" -Headers @{ Authorization = "Basic $token" }
```

If the app is running in Docker, use port `8081` instead.

## Frontend integration notes

The backend currently supports browser calls from `http://localhost:3000`.

To allow multiple frontend origins before starting the backend:

```powershell
$env:CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:5173"
```

Because the API currently uses **Basic Auth headers**, frontend requests should send the `Authorization` header. Cookie-based auth is not configured.

Example frontend request:

```javascript
const credentials = btoa('username:password');

const response = await fetch('<localhost:url>/api/v1/users/user', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Basic ${credentials}`
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

- `CreateUserRequest` accepts `password`, and also supports alias `passwordHash`
- `CreateUserRequest` accepts `telephoneNumber`, and also supports alias `phoneNumber`
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

## Build and test

Build the jar:

```powershell
.\gradlew.bat bootJar
```

Run all tests:

```powershell
.\gradlew.bat test
```

The project includes PostgreSQL integration tests via Testcontainers.

## Troubleshooting

### No generated Spring Security password in logs

This is expected. The app defines its own in-memory user in `SecurityConfig`, so Spring Boot does not generate or print a random password.

### Application fails with datasource configuration errors

Make sure PostgreSQL is running and these values are correct for your environment:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`

### Docker app cannot connect to PostgreSQL

Inside Docker Compose, the app should connect using the configured container host from `.env`, typically the PostgreSQL service name.

### Browser request blocked by CORS

Make sure the frontend origin is included in `CORS_ALLOWED_ORIGINS` or leave the default `http://localhost:3000`.

## Postman note

There is a `postman/README.md` file with additional Postman notes. If you want to keep Postman import files in the repository, place them in the `postman/` folder and document them there.
