# Saving Planner

A Spring Boot + Kotlin API for savings planning and budgeting.

## Docker local development

This project now includes a complete Docker-based local development setup for:

- the Spring Boot application
- a PostgreSQL database

Files added for Docker:

- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`

### What you are learning

When you run this stack with Docker, you are learning four useful ideas:

1. **Image** – a packaged build of your app
2. **Container** – a running instance of that image
3. **Compose** – a way to run multiple services together
4. **Service networking** – containers can talk to each other by service name

In this project:

- the app runs in one container
- PostgreSQL runs in another container
- the app connects to the database using `DB_HOST={hostname of the PostgreSQL container}`

That works because name is the Compose service name.

## Start the full stack

From the project root:

```powershell
from you project root
docker compose up --build
```

What this does:

- builds the Spring Boot Docker image from the `Dockerfile`
- starts PostgreSQL
- waits for PostgreSQL health check to pass
- starts the app container

### URLs and credentials

App:

- `http://localhost:8081` or `http://localhost:8080`

PostgreSQL from your host machine:

- host: `localhost`
- port: `db_port`
- database: `db_name`
- username: `db_username`
- password: `db_password`

App HTTP Basic login:

- username: `app_username`
- password: `app_password`

> Note: PostgreSQL is exposed on host port `5434` on purpose, so it does not clash with your existing local PostgreSQL running on `5433`.
> The Spring Boot app is exposed on host port `8081` so it can run at the same time as a host-based app on `8080`.

## Run in the background

```powershell
docker compose up --build -d
```

## Stop the stack

```powershell
docker compose down
```

## Stop the stack and remove the database volume

```powershell
docker compose down -v
```

Use `-v` only if you want a fresh PostgreSQL database.

## See logs

All logs:

```powershell
docker compose logs -f
```

Only app logs:

```powershell
docker compose logs -f app
```

Only PostgreSQL logs:

```powershell
docker compose logs -f postgres
```

## Test the running app

Open Postman and use:

- base URL: `http://localhost:8081` or `http://localhost:8080`
- Basic Auth username: `app_username`
- Basic Auth password: `app_password`

Or test quickly from PowerShell:

```powershell
$pair = 'username:password'
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$token = [Convert]::ToBase64String($bytes)
Invoke-WebRequest -Uri "http://localhost:8081/api/v1/users" -Headers @{ Authorization = "Basic $token" }
```

## Frontend integration notes

The backend allows browser calls from `http://localhost:3000` by default.

You can override the allowed origin list before starting the backend:

```powershell
$env:CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:5173"
```

Example browser `fetch(...)` call using the current Basic Auth setup:

```javascript
const credentials = btoa('username:password');

const response = await fetch('http://localhost:8081/api/v1/users/user', {
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
    onboardingDone: true
  })
});
```

Because the API currently uses Basic Auth headers rather than cookie-based login, you do not need `credentials: 'include'` for this request style.

## Useful Docker commands to learn

Show running containers:

```powershell
docker ps
```

Show built images:

```powershell
docker images
```

Open a shell inside the app container:

```powershell
docker exec -it saving-planner-app sh
```

Open a shell inside PostgreSQL container:

```powershell
docker exec -it saving-planner-postgres sh
```

Connect to PostgreSQL inside the container:

```powershell
docker exec -it saving-planner-postgres psql -U postgres -d saving_planner
```

## Recommended learning path

### Step 1: Run only PostgreSQL in Docker

If you want to learn gradually, start with only the database:

```powershell
docker compose up -d postgres
```

Then run your Spring Boot app from IntelliJ or Gradle using:

```text
- host: `localhost`
- port: `db_port`
- database: `db_name`
- username: `db_username`
- password: `db_password`
```

This teaches you how a host app connects to a containerized database.

### Step 2: Run the whole stack in Docker

Then move to:

```powershell
docker compose up --build
```

This teaches you how app-to-db communication works entirely inside Docker.

## Common issues

### Port 8080 already in use

Stop the process using port 8080, or change the app mapping in `docker-compose.yml`:

```yaml
ports:
  - "8082:8080"
```

### Port 5434 already in use

Change this in `docker-compose.yml`:

```yaml
ports:
  - "5435:5432"
```

### App starts before DB is ready

This setup already includes a PostgreSQL health check and waits before starting the app.

### Want a fresh DB

```powershell
docker compose down -v
docker compose up --build
```

## Next improvements you can learn later

After you are comfortable with this setup, good next topics are:

- Spring Boot profiles like `application-docker.properties`
- Flyway or Liquibase migrations
- Docker volumes and bind mounts
- live reload / dev-only compose overrides
- CI pipelines that run tests in containers

