# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Java app for pet owners (log vet visits and treatments, find nearby pet services, get food recommendations). It is a university project (Java, databases and entrepreneurship courses) and a portfolio piece, built to industry standards. **Early setup:** a Spring Boot 4.1.x skeleton (`PetOwnerApplication` and a context-loads test), a placeholder `Greeter` class and its test exist. The app listens on port 8080 and has no endpoints yet. No domain code exists yet. See Architecture rules below for constraints on what gets built.

## Commands

Use the Maven Wrapper; Maven is not installed separately. Requires JDK 25.

The database must be running (`docker compose up -d`) before `.\mvnw.cmd verify` or `test`: the context-loads test and `DatabaseConnectionTest` connect to PostgreSQL.

- Build + test: `.\mvnw.cmd test` (Windows) / `./mvnw test`
- Full CI-equivalent build: `./mvnw -B verify` (this is what `.github/workflows/ci.yml` runs on Ubuntu, Temurin 25)
- Run the app: `.\mvnw.cmd spring-boot:run` (port 8080, no endpoints yet)
- Single test class: `.\mvnw.cmd test -Dtest=GreeterTest`
- Single test method: `.\mvnw.cmd test -Dtest=GreeterTest#greetIncludesTheName`

No linter or formatter is configured.

## Stack and layout

- Target stack: Java 25, Maven, Spring Boot 4.x, PostgreSQL 18 through Spring's `JdbcTemplate`, a REST API with token authentication, a Leaflet/OpenStreetMap map later. Base package `io.github.edymihai.petowner`; `maven.compiler.release` is 25. Current dependencies: `spring-boot-starter-webmvc`, `spring-boot-starter-jdbc`, the PostgreSQL driver (runtime) and, in test scope, `spring-boot-starter-webmvc-test` and `spring-boot-starter-jdbc-test`. `application.properties` imports `.env` (`spring.config.import=optional:file:.env[.properties]`) and builds the datasource from `POSTGRES_*`; CI supplies dummy `POSTGRES_*` values and a `postgres:18` service.
- PostgreSQL 18 runs via `compose.yaml` (`docker compose up -d`). Credentials come from `.env` (copy `.env.example`), and compose fails fast if any `POSTGRES_*` variable is missing. The port is bound to `127.0.0.1:5432` only. Data lives in the `pgdata` volume (`docker compose down -v` deletes it). `.env` must never be committed.
- `.gitattributes` forces LF for `mvnw` and CRLF for `*.cmd`. Keep it that way, or the wrapper breaks on the other OS.
- Dependabot (`.github/dependabot.yml`) opens update PRs for Maven and GitHub Actions. Commit messages follow Conventional Commits (`chore(deps): ...`, `ci: ...`).
- `.claude/settings.json` is shared and denies reading and editing `.env`. `.claude/settings.local.json` is personal (permission mode, "always allow" rules) and is git-ignored.

## Architecture rules (apply to all new code)

- Spring Boot and PostgreSQL are used from the start. Check the Spring Boot 4.x documentation before writing code: many tutorials still target 3.x, and 4.x has breaking changes (Spring Framework 7, Jakarta EE 11).
- Domain classes must not depend on Spring or on any storage technology. Storage sits behind interfaces named after the domain (for example `VisitLog`), not generic `Repository` / `Manager` / `Service` names, so one storage implementation can be replaced by another without touching the domain.
- Database access uses `JdbcTemplate` with plain SQL. Do not use Spring Data JPA or Hibernate: the owner must be able to explain every SQL statement in the databases course defence.
- The class diagram is graded in the Java course: at least 4 classes, 2 interfaces and 2 collections used by the application, with relationships that match the code. The file requirement is met by a small feature that writes to and reads from a file.
- Package-by-feature under `io.github.edymihai.petowner`. Keep classes small and single-purpose.

## Workflow

- Never commit to `main`. One small task per branch (`feature/...`, `fix/...`, `docs/...`, `ci/...`), merged by pull request. Check `git branch --show-current` before committing.
- Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `ci:`, `chore:`. Small, focused commits.
- Plan first: for any feature, propose a plan and wait for approval before editing files.
- Write or update tests with the code. Run `.\mvnw.cmd -B verify` and report the result before saying a task is done.
- Do not commit or push unless asked. Show `git diff` for review first.

## Explaining the code

The owner presents and defends all code in class and may not know every concept yet. When writing code, explain the design choice in a few sentences and name the Java and Spring concepts used (interfaces, collections, generics, exceptions, I/O, dependency injection). Finish each task with a short list of what the owner should be able to explain, and explain each item in plain language with a small example.

## Do not

- Do not read, print or edit `.env`. Use `.env.example` as the reference. Never put passwords or tokens in code, tests, commits or logs.
- Do not add or upgrade dependencies, change the Maven or Java version, or edit `compose.yaml` or `.github/` without asking first.
- Do not write the database schema, migrations, constraints, views or business queries for the databases course unless asked; the owner writes and defends those. When Java code needs SQL, propose the structure and let the owner write the statement.
