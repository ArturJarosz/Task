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

- **Validators** extend `BaseValidator` and use static assertion methods (`assertNotNull`, `assertIsTrue`, etc.)
- **Mappers** use MapStruct with Spring component model (configured in `pom.xml`)
- **Application services** are annotated with `@ApplicationService` (custom stereotype)
- **Domain services** contain pure business logic, no framework dependencies
- **Repositories** use Spring Data JPA
- Exception messages codes are created with method
  `com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode` and translated with i18n. Translation
  for them is located in `core/src/main/resources/i18n` and split into separate files for each module.

## Testing

- Unit tests use **Spock Framework** (Groovy), located in `src/test/groovy`
- Integration tests (`*IT.groovy`) extend `BaseTestIT` which uses **Testcontainers** for PostgreSQL. They are devided
  into
  separate modules by domains. Each new API method should be covered by integration test.
- Test naming: `*Test.groovy` for unit tests, `*IT.groovy` for integration tests

## Tech Stack

- Java 17, Spring Boot 3.1
- PostgreSQL with Liquibase migrations
- QueryDSL for type-safe queries
- MapStruct Lombok for mapping
- Auth0/Okta for authentication
