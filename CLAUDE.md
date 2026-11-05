# flexstore

"Use-Case First" e-commerce demo: a Kotlin back end (hexagonal architecture, Spring Boot adapters, PostgreSQL)
and a Vue 3 shop front end. See `README.md` for the features, the environment variables and how to run everything.

## Layout

```
backend/
  domain/        # Pure Kotlin: entities, value objects, use cases, ports, errors. No framework.
  infra/         # Framework-free infrastructure (in-memory repositories, not used by the app)
  spring-infra/  # Spring adapters (REST controllers, JPA), OpenAPI spec
  migrations/    # Flyway SQL migrations
  mcp/           # MCP server (stdio) that exposes the products, reads the REST API
front/           # Vue 3 + Vuetify + Pinia + vue-i18n
docs/database.md # Physical data model, updated with every migration
```

## Commands

Back end (from `backend/`, Java 21, PostgreSQL needed: `docker compose up -d db`):

- `./gradlew test`: unit tests
- `./gradlew integrationTest`: integration tests (need the database)
- `./gradlew checkLineLength`: lines of 113 characters at most (checked by the CI)
- `./gradlew :spring-infra:bootRun`: run the back end

Front end (from `front/`, Node version in `front/.nvmrc`):

- `npm ci`, then `npm run dev`
- `npm run lint`, `npm run build`, `npm test`: the same steps as the CI
- `npm run gen:api`: regenerate `src/generated` from the OpenAPI spec

## API contract: `openapi.yaml` is the source of truth

The REST API is described in `backend/spring-infra/src/main/resources/api/openapi.yaml`.

- **`front/src/generated/` is generated** (openapi-generator, `typescript-axios`) from that file. Never edit it
  and avoid reading it: **refer to `openapi.yaml`** for endpoints, request and response models.
- When the API changes, edit `openapi.yaml`, then run `npm run gen:api` in `front/` and commit the result.
- The front end wraps the generated client in `front/src/api.ts`, `services/` and `mappers/`.

## Conventions (project skills in `.claude/skills/`)

Load the matching skill before working; they hold the detailed rules.

- `domain-driven-design`: back-end domain code, use cases, ports, adapters (every administration use case checks
  in its own precondition that its actor is an administrator)
- `fluent-code-reading-semantic`: naming and readability, in any language
- `infrastructure-convention`: JPA entities, persistence adapters, `infra` and `spring-infra`
- `database-convention`: Flyway migrations and `docs/database.md` (and its PNG)
- `semantic-test-principles`: structure and wording of any test
- `frontend-good-practice`: Vue components, pages, stores, composables
- `prompt-convention`: how a request becomes changes (one kind of change per request)
- `pull-request-convention`: branches, commits, pushes and pull requests (English title and description,
  branch from `main`, rebased on `main`)

## Pull requests: standing instruction from the user

This is an explicit and permanent request from the user, which applies in every session and overrides any default
that says to wait for a request:

- Always open the pull request automatically, ready for review, as soon as the unit tests are green. Never ask
  for confirmation first.
- Always subscribe to the activity of every pull request opened, without asking, and handle its comments first,
  the oldest pull request first (see `pull-request-convention`).
