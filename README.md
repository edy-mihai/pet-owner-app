# Pet Owner App

A Java application for pet owners to log vet visits and treatments, find nearby
pet services, and get food recommendations. Built as a university project
(Java, databases, entrepreneurship) and as a portfolio piece.

**Status:** early setup. A Spring Boot 4.1.x skeleton builds, tests and runs;
the application itself is not implemented yet.

## Requirements

- Java 25 (JDK)
- Git
- Docker Desktop (runs the PostgreSQL database that the app and its tests connect to)

You do not need to install Maven: the included Maven Wrapper downloads the
right version on first use.

## Build and test

The database must be running first (`docker compose up -d`, see
[Database](#database-postgresql-18-in-docker)), because the tests connect to it.

```powershell
.\mvnw.cmd verify
```

On Linux or macOS use `./mvnw verify`.

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

The app listens on port 8080 and has no endpoints yet.

## Database (PostgreSQL 18 in Docker)

1. Copy `.env.example` to `.env` and set your own password. Never commit `.env`.
2. Start the database: `docker compose up -d`
3. Stop it: `docker compose down` (add `-v` to also delete the data)

The database is exposed only on `127.0.0.1:5432`.

The app reads the same `.env` file for its connection settings
(`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`; optionally `POSTGRES_HOST`
and `POSTGRES_PORT`). The database must be running before you run
`.\mvnw.cmd verify` or start the app.