# Pet Owner App

A Java application for pet owners to log vet visits and treatments, find nearby
pet services, and get food recommendations. Built as a university project
(Java, databases, entrepreneurship) and as a portfolio piece.

**Status:** early setup. The build and a sample test work; the application
itself is not implemented yet.

## Requirements

- Java 25 (JDK)
- Git
- Docker Desktop (only for the PostgreSQL database, used later in the project)

You do not need to install Maven: the included Maven Wrapper downloads the
right version on first use.

## Build and test

```powershell
.\mvnw.cmd test
```

On Linux or macOS use `./mvnw test`.

## Database (PostgreSQL 18 in Docker)

1. Copy `.env.example` to `.env` and set your own password. Never commit `.env`.
2. Start the database: `docker compose up -d`
3. Stop it: `docker compose down` (add `-v` to also delete the data)

The database is exposed only on `127.0.0.1:5432`.