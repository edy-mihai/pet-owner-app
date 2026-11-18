# Pet Owner App

A Java application for pet owners to log vet visits and treatments, find nearby
pet services, and get food recommendations. Built as a university project
(Java, databases, entrepreneurship) and as a portfolio piece.

**Status:** mid-term version. A console program (Java, in-memory storage, no
database needed) manages accounts, pets and co-owners, care plans with a
"today" list and "mark as given", and exports a pet's history to a text file.
Vet visits, the database storage, the REST API and the map are not built yet.
A Spring Boot 4.1.x skeleton with a PostgreSQL connection builds, tests and
runs, but has no endpoints yet.

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

## Run the console app

The console program is the Java mid-term demo. It needs no database and no
Spring. All data is kept in memory and is lost when you exit.

```powershell
.\mvnw.cmd -B clean compile
java -cp target\classes io.github.edymihai.petowner.console.ConsoleApp
```

Optional arguments:

- `--demo`: load sample accounts, pets and care plans (default: off, starts empty).
- `--export-dir=PATH`: folder for exported pet histories (default: `exports/`).
- `--color`: force colors on, even without a real terminal (default: automatic).
- `--no-color`: turn colors off; wins over `--color` (default: automatic).

Example: `java -cp target\classes io.github.edymihai.petowner.console.ConsoleApp --demo --no-color`

Exports go to `exports/` unless you pass `--export-dir`. Colors are on only in
a real terminal (and off if the `NO_COLOR` environment variable is set), unless
you force them with `--color`. An unknown argument prints the usage and exits.

## Database (PostgreSQL 18 in Docker)

1. Copy `.env.example` to `.env` and set your own password. Never commit `.env`.
2. Start the database: `docker compose up -d`
3. Stop it: `docker compose down` (add `-v` to also delete the data)

The database is exposed only on `127.0.0.1:5432`.

The app reads the same `.env` file for its connection settings
(`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`; optionally `POSTGRES_HOST`
and `POSTGRES_PORT`). The database must be running before you run
`.\mvnw.cmd verify` or start the app.

## Windows notes

### Docker Desktop on Windows 10 Home (WSL2)

Windows 10 Home has no Hyper-V, so Docker Desktop needs WSL2. In a PowerShell
opened as administrator run:

```powershell
wsl --install --no-distribution
```

Then restart the computer before starting Docker Desktop.

### Low-RAM machines (optional)

WSL2 can take a large share of memory. To cap it, create
`C:\Users\<name>\.wslconfig`:

```ini
[wsl2]
memory=2GB
processors=2
```

Then apply it with `wsl --shutdown` and start Docker Desktop again.

### Installing Java 25 next to another JDK

```powershell
winget install EclipseAdoptium.Temurin.25.JDK
```

Open a **new** terminal (old ones keep the old environment) and check:

```powershell
java -version
echo $env:JAVA_HOME
```

`java -version` must report 25, and `JAVA_HOME` must point to the Temurin 25
folder. If either still shows the other JDK, update `JAVA_HOME` and the `Path`
entry in the Windows environment variables.

### Order of commands

The database must be running before the build, because the tests connect to it:

```powershell
docker compose up -d
.\mvnw.cmd -B verify
```
