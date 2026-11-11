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
- **Mappers** use MapStruct with Spring component model (configured in `pom.xml`).
- **Application services** are annotated with `@ApplicationService` (custom stereotype)
- **Domain services** contain pure business logic, no framework dependencies
- **Repositories** use Spring Data JPA
- **Lists configured by users that are only a name** (project, stage and task types, contractor, supplier and cost
  categories) build on `sharedkernel/namedentry`: the entity extends `AbstractNamedEntry`, the repository
  `NamedEntryRepository`, the validator `NamedEntryValidator` and the application service
  `AbstractNamedEntryApplicationService`. Check that an item's entry exists with `validateExistence(id)`, which locks the
  entry until the transaction ends, so it cannot be deleted while the item is being saved.
- **Roles and permissions**: what a user may do is a set of `Permission`s (`security/permission`), each an `Action`
  (read, create, edit, delete) on a `Resource` (client, project...). Money is kept apart: `PROJECT` is the work on a
  project, `PROJECT_FINANCE` its money and `FINANCIAL_REPORT` the company-wide report. A resource lists the actions
  available for it and the resource it is part of; any other action needs Read, and a part needs Read of the whole
  (`Permission.getRequiredPermissions`). A new resource or action also goes into the `PermissionResource` or
  `PermissionAction` enum of the OpenAPI spec (`PermissionMapperTest` fails until it does) and needs a label in the
  frontend. They are read only through the `PermissionProvider` interface. The default `DatabasePermissionProvider` takes them from the roles of the local
  user (`Role`, a named entry with permissions; `User.roleIds`); another source can be plugged in with the
  `task.security.permission-source` property. Administrator is the only predefined role and always has every
  permission, so a new `Permission` reaches it without a migration; other roles are created by users in Settings.
  The current user's permissions are returned by `GET /users/current`; endpoints do not check them yet.
- Exception messages codes are created with method
  `com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode` and translated with i18n. Translation
  for them is located in `core/src/main/resources/i18n` and split into separate files for each module.
- **No `@Value` for config** — use `@ConfigurationProperties` records (ArchUnit-enforced).
- **No reactive or async.** The app is synchronous.
- **Do not commit by default** — only when explicitly asked.
- **YAGNI / KISS / prefer existing libraries.** If a nice-to-have turns out hard to get right, question it or remove it.
  Get user approval before adding a new
  dependency.
- **Run a quick self-review against these rules after any change.**
- **Act like a senior dev: surface assumptions, stay surgical, verify.**
- **Fix problems at their source, and build new things the way their neighbours are built.**
- **Comments must be true, explain why, and survive the cover test.**
- **Follow "Effective Java" (Joshua Bloch).**
- **Consistency over novelty.** When in doubt, match the surrounding code. Before adding something new of a common
  kind — a table, an endpoint's permissions, an
  enum, a config key, error handling — look at the two closest examples in the repo and do it their way, or say in the
  commit message why their way does not
  fit.
- **`Optional`:**
    - Don't use `Optional.ofNullable()` for simple null checks — use an explicit null check.
    - `isPresent()` + `get()` is an antipattern — use `map` / `orElse` / `ifPresent` / etc.
    - For default-if-null, prefer `MoreObjects.firstNonNull()` (Guava) or `Objects.requireNonNullElse()` (JDK).
    - For a list, set, collection or map that may be null, use Commons Collections' `emptyIfNull`, statically imported
      from `ListUtils`, `SetUtils`,
      `CollectionUtils` or `MapUtils`, not `requireNonNullElse(list, List.of())`.
- **Return results, don't mutate parameters.** A method that fills a caller-supplied collection hides its output; build
  and return the collection instead.
  Mutating a parameter is the contract only where accumulation is the method's stated purpose, and then the name must
  say so.
- **Modern Java idioms:**
    - Use records, sealed types, pattern matching, switch expressions, and streams where they improve readability —
      don't force them.
    - Prefer `SequencedCollection` methods (`getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `removeFirst()`,
      `removeLast()`, `reversed()`) over
      index-based equivalents.
    - Prefer `IO.println` over `System.out.println`.
    - Use `_` for unused lambda parameters (`(_, v) -> process(v)`) and unused catch parameters (`catch (Exception _)`).
    - Prefer switch expressions over switch statements; prefer switch expressions with pattern matching over long `if`/
      `else if` chains with `instanceof`.
    - Use the compact `main` method syntax where applicable (instance `main`, no `String[] args`, no `static`).
    - Prefer factory methods: `Locale.of(...)` over `new Locale(...)`, `Integer.valueOf(...)` over `new Integer(...)`,
      etc.
    - Prefer less verbose API overloads (e.g., `new FileOutputStream("file.txt")` over
      `new FileOutputStream(new File("file.txt"))`).
    - Prefer `Reader.of(string)` over `new StringReader(string)`.
    - Omit trailing `;` on enum constants when the enum has no constructors, fields, or methods.
- **Loop bodies stay simple.** Long `continue`/`if` ladders inside a loop usually mean the per-element logic should be
  extracted to a method, expressed as a
  `stream().filter(...).forEach(...)` pipeline, or replaced with a `switch` expression. A single `break` or an
  occasional guard `continue` is fine — the target
  is branching complexity, not the keywords.

#### Naming

- Packages: lowercase, `^[a-z]+(\.[a-z][a-z0-9]*)*$`.
- Lambda parameters: camelCase starting lowercase (or `_` if unused).
- Type parameters: single uppercase letter or uppercase word ending with `T`.
- **The test for every name:** a reader who sees only the name and the signature should be able to guess what the code
  does. If reading the body surprises them,
  the name is wrong. Names that are read without the body — methods, fields, parameters, record components — must be
  exact. A local that lives for a few lines
  may be short (`i` in a small loop is fine), but even a short name must never say something false. The common mistakes:
    - **Name what the thing is now, not what happens to it later.** A pair built while planning, before anything ran, is
      an `OperationAssignment`, not an
      `AnsweredOperation` — nothing is answered yet. A flag meaning "must still be computed" is `needsCompute`, not
      `computed`.
    - **A word that names a type in this module means that type.** If `plan` is an `ExecutionPlan`
      here, don't call a variable of another type `plan` — the reader resolves the word to the type. Plain words with no
      type behind them (`measure`, `value`,
      `result`) are free to reuse. Related smell: an access chain that reads like `entry.entry()` — one of the two names
      says nothing; name the component by
      what it holds (→ `entry.request()`).
    - **A rename is finished when the old word is gone.** After renaming a type or concept, rename the locals,
      parameters and lambda variables still carrying
      the old word — they now claim the old meaning.
    - **Methods sharing a verb must mean the same thing by it.** If every `supports*` method means
      "this class can handle X", don't use it for something else: `supportsEmptyResult` meant
      "the result is built empty, no call needed" → `producesEmptyResult`. If `serve*` methods do the work, a method
      that only checks must not be called
      `serves`. And when siblings split a topic between them, a bare `supports` sounds like it covers all of it — name
      the part this one actually answers for.
    - **Name what the method does for its caller, not an inside detail.** A method that adds each group's weights into a
      shared map and reports an instrument
      found in two groups is
      `mergeGroupWeights` — not `duplicateKeyError`, which named the error text it happens to build.
    - **`xToY` names a conversion**, an X in and a Y out. A method that returns an X for some purpose is named after the
      X:
      `requestedPortfolio(request)`, not `portfolioToHedge(request)`.
    - **A method that does work is named for the work, with a verb**, even when it returns the outcome: `runWithHedge`,
      not `variant`. A noun (`requestedPortfolio`) says that the method only builds or looks something up. So a name
      that starts with a verb must do that verb: a method
      that returns the permissions a configuration key needs is `permissionsRequiredToEdit(key)`, not
      `editPermissions(key)`, which reads as if it
      edited them.
    - **A word from the ticket's prose is not a name.** A builder of a `HedgeEffectivenessScenarioResult` is
      `scenarioResult`, not `figures`. Domain
      vocabulary that names a type stays.
    - **Avoid vague describing words.** `shaped`, `involved`, `proper` — if you cannot say which fact makes the word
      true, use the fact as the name:
      `involvedGroups` → `groupsToCall`.
    - **Don't hide extra checks.** If a boolean method checks more than its name says — exactly one element, a specific
      type, false for an empty list — work it
      into the name. Where that would make the name unreadable, the javadoc's first sentence must state it: a condition
      the name cannot carry is the method's
      contract, not the repeated *what* that **Comments & Javadoc**
      rules out.
    - **If the javadoc explains the name in better words, make those words the name.** A method documented as "the
      deviation of actual from target" but named
      `relativeValue` is
      `relativeDeviation`.

### Agent Behavior

Operate like an experienced senior engineer on this codebase: pragmatic, skeptical of unnecessary work, biased toward
the simplest change that provably solves
the problem. Prefer asking over guessing, and verifying over declaring done.

- **Surface assumptions.** If the request is ambiguous or has multiple reasonable interpretations, list them and ask —
  don't silently pick one. If a simpler
  approach exists, say so and push back when warranted.
- **Stay surgical.** Every changed line must trace to the request. No drive-by refactors, reformatting, or "while I'm
  here" cleanups. Match existing style even
  if you'd do it differently. Remove only orphans your own change created; mention unrelated dead code instead of
  deleting it.
- **Verify, don't declare.** Turn tasks into verifiable goals: bugs → reproduce with a failing test first; features →
  define the success check before coding;
  multi-step tasks → brief plan with a verify step per item. Strong success criteria let you finish without a second
  round-trip.
- **Senior-dev gut check.** Before submitting, ask: *"Would an experienced engineer on this team call this
  overengineered, out-of-scope, or inconsistent with
  the codebase?"* If yes, trim or rework.

> These behaviors bias toward caution over speed. For trivial tasks, use judgment.

### Architecture Principles

#### Configuration Properties

- Define config as Java `record`s annotated with `@ConfigurationProperties`.
- Do not use `@Value`.

#### Dependency Injection

- **Constructor injection is the standard**: Lombok `@RequiredArgsConstructor` + `private final` fields.
- **No field `@Autowired` in production code** (tests may use field injection).

## Liquibase Changesets

- Never use raw SQL (a `sql` change or a `sqlCheck` precondition) when a Liquibase change or precondition can do the
  same, e.g. `createIndex` with a `computed: true` column for `lower(NAME)`, or `changeSetExecuted` to check whether
  another changeset ran. Older changesets that use raw SQL are no reason to copy it.
- A change only PostgreSQL supports (an index on an expression) gets `dbms: "postgresql"` on its changeset instead of
  raw SQL: the CI `liquibase_test` job runs the changelog on H2, with the tables in `TASK_SCHEMA`.
- Check a new changeset on H2 the way CI does with `cd database && mvn spring-boot:run -Dspring-boot.run.profiles=test`,
  and remove the `taskdb.*` files it leaves.
- Never edit a changeset that may already have run; add a new one.

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
