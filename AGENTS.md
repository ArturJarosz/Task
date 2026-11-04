# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) and other AI code tools when working with code in this
repository.

## Build and Test Commands

```bash
# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests only
mvn test

# Run a single test class
mvn test -pl core -Dtest=ProjectValidatorTest

# Run a single test method
mvn test -pl core -Dtest=ProjectValidatorTest#"test method name"

# Build fat JAR for deployment
mvn clean install -PfatJar

# Generate OpenAPI sources (happens during compile)
mvn compile -pl core
```

## Docker Development

```bash
cd docker

# Start full environment (database  backend  frontend)
./app-up.sh -e local -v 0.6.2-SNAPSHOT -b full

# Start only-run mode (uses existing database)
./app-up.sh -e local -v 0.6.2-SNAPSHOT -b only-run

# Help
./app-up.sh -h
```

Environment variables are configured in `docker/env/local.env` (use `docker/env/example.env` as template).

## IntelliJ Run Configurations

Pre-configured run configurations in `.run/`:

1. `Drop and Init DB` - Reset database schema
2. `Load sample data` - Populate with test data
3. `Run Task BE` - Start backend application
4. `Update DB` - Apply Liquibase migrations

## Project Architecture

### Module Structure

- **sharedkernel** - Base classes and infrastructure shared across modules:
    - `model/` - Abstract entities (`AbstractAggregateRoot`, `AbstractEntity`), value objects (`Money`, `Email`,
      `Address`)
    - `exceptions/` - Exception hierarchy and `BaseValidator` for validation
    - `status/` - Generic workflow/state machine infrastructure (`Workflow`, `WorkflowAware`, `StatusTransition`)

- **core** - Main application with domain modules
- **database** - Liquibase migrations and database configuration
- **sample-data** - Test data generators

### Domain-Driven Design Layers

Each domain module (project, contract, finance, etc.) follows this structure:

```
domain-name/
├── application/     # Application services (use cases), validators, mappers
├── domain/          # Domain services and business logic
├── model/           # JPA entities and aggregates
├── infrastructure/  # Repository implementations
├── query/           # Read-only query services
├── rest/            # REST controllers (implement generated interfaces)
└── status/          # State machine for domain-specific workflows
```

### API-First Development

REST API is defined in `core/src/main/resources/openapi/openapi-spec.yml`. During compilation:

- DTOs are generated to `com.arturjarosz.task.dto` package (with `Dto` suffix)
- Controller interfaces are generated to `com.arturjarosz.task.rest` package
- Controllers implement these generated interfaces

### Status Workflow Pattern

Entities with lifecycle states (Project, Stage, Task, Contract) use a generic workflow system:

- `Workflow<T extends Status>` - Defines valid states and initial state
- `WorkflowAware<T>` - Interface for entities with status
- `StatusTransition<T>` - Defines allowed state transitions
- `StatusTransitionListener` - Hooks for side effects on state changes

Example: `ProjectWorkflow` defines TO_DO → IN_PROGRESS → DONE flow with validators and listeners.

### Key Patterns

- **Validators** extend `BaseValidator` and use static assertion methods (`assertNotNull`, `assertIsTrue`, etc.).
- **Mappers** use MapStruct with Spring component model (configured in `pom.xml`)
- **Application services** are annotated with `@ApplicationService` (custom stereotype)
- **Domain services** contain pure business logic, no framework dependencies
- **Repositories** use Spring Data JPA
- **Lists configured by users that are only a name** (project, stage and task types, contractor, supplier and cost
  categories) build on `sharedkernel/namedentry`: the entity extends `AbstractNamedEntry`, the repository
  `NamedEntryRepository`, the validator `NamedEntryValidator` and the application service
  `AbstractNamedEntryApplicationService`. Check that an item's entry exists with `validateExistence(id)`, which locks the
  entry until the transaction ends, so it cannot be deleted while the item is being saved.
- Exception messages codes are created with method
  `com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode` and translated with i18n. Translation
  for them is located in `core/src/main/resources/i18n` and split into separate files for each module.

## Release Notes

After logging in, users see a "What's new" popup listing the changes made since their last visit. The entries are rows
of the `RELEASE_NOTE` table, and each one is added by a Liquibase changeset.

- Every story or bugfix that changes what users see or can do adds one note (two only for clearly separate changes).
  Work that users don't notice adds none: dependency updates, CI, refactoring, test-only or API-only changes.
- A story made only in the frontend still adds its note here.
- Put the note in `database/src/main/resources/database/changelogs/v<version>/release-note/` as
  `add-release-note-<ticket>.yml` (for example `add-release-note-ta-585.yml`). Include it at the end of that folder's
  `release-note-tables.yml`. Copy `v0.6.2/release-note/add-release-note-ta-589-whats-new.yml`, which has the changeset format.
- Write for the people using the app, not for developers:
    - Describe what they can now do or what works differently. Never mention endpoints, fields, tables or class names.
    - Use plain English in the second person ("you"), with sentence case and no exclamation marks.
    - `TITLE` is at most 80 characters and has no full stop, for example "Create your own project types".
    - `DESCRIPTION` is one or two sentences, at most 1000 characters, that say where to find the change or what it
      means for the user.
    - `TYPE` is `NEW` for something users could not do before, `IMPROVED` for a change to something that already
      existed, and `FIXED` for a bugfix.
    - `TICKET` is the story key, for example `TA-585`. It is never shown to users.
    - `APPLICATION_VERSION` is the version the change is released with, without `-SNAPSHOT`, matching the `v<version>`
      folder, for example `0.6.2`. "What's new" groups the notes by it.
- Never edit a changeset that may already have run. To fix the text of a note, add an `update` changeset.

## Testing

- Unit tests use **Spock Framework** (Groovy), located in `src/test/groovy`
- Integration tests (`*IT.groovy`) extend `BaseTestIT` which uses **Testcontainers** for PostgreSQL. They are devided
  into
  separate modules by domains. Each new API method should be covered by integration test.
- Test naming: `*Test.groovy` for unit tests, `*IT.groovy` for integration tests

## Tech Stack

- Java 25, Spring Boot 4.1
- PostgreSQL with Liquibase migrations
- QueryDSL for type-safe queries
- MapStruct Lombok for mapping
- Auth0/Okta for authentication
