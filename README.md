# flexstore

> **Use-Case First** e-commerce demo: a **Kotlin** back end with a clean hexagonal architecture (Domain ↔ Application ↔ Infrastructure) and Spring Boot adapters, and a **Vue 3** shop front end.

Live demo: https://flexstore-front.pages.dev/ (the back end runs on Render's free plan: the first request after a while can take some time while it wakes up).

---

## 🚀 Why

`flexstore` is a minimal, opinionated template to promote **programming oriented around use cases**. Each user journey is implemented as a small, pure **application service** with:

- Explicit **REST request/response models (Jackson) mapped to pure use-case inputs/outputs**
- **Ports** (interfaces) that abstract infrastructure
- **Adapters** (REST, persistence, external APIs) that implement those ports

This repo is meant to be a learning vehicle and a ready-to-fork base for real projects.

---

## 🛍️ Features

**Shop** (FR/EN)
- Home page: categories, a selection of products, the shop values and commitments
- Catalog: product cards, category filter (clothing, electronics, accessories), sort by name or price
- Product detail: description, quantity, add to cart, products of the same category
- Cart: quantity per line, remove, total, item count badge in the header

**Account and administration** (sign-in required)
- Sign in, profile
- Users: list, create, delete
- Products: create

Orders are demo data: they are not stored by the back end yet.

---

## 🧱 Architecture (Hexagonal)

**Domain**: pure Kotlin (entities, value objects, use cases, ports, domain errors). No framework.\
**Infrastructure**: Spring Boot adapters implementing ports (REST controllers as *input adapters*, JPA as *output adapter*).

Users are stored in PostgreSQL (JPA). Products and the cart are kept in memory (`InMemoryProductRepository`, `InMemoryItemRepository`): they are reset when the back end restarts.

---

## 📦 Modules

```
backend/
  app/            # not used at the moment
  domain/         # Entities, value objects, use cases, ports, errors
  infra/          # Infrastructure not linked to a framework (in-memory repositories)
  spring-infra/   # Spring adapters (REST controllers, JPA), OpenAPI spec
  migrations/     # Flyway SQL migrations
front/            # Vue 3 + Vuetify + Pinia + vue-i18n shop
```

---

## ⚙️ Quickstart

### 1. Database

The back end needs PostgreSQL. Start it with Docker (the credentials come from the root `.env` file):

```bash
docker compose up -d db
```

Flyway migrations run when the back end starts. To open a SQL console:

```bash
docker exec -it flexstore-db psql -U flex -d flexstore
```

### 2. Back end

**Prerequisites**: Java 21 (the Gradle wrapper is included).

```bash
cd backend
export FLEXSTORE_DATABASE_URL=postgresql://flex:flex@localhost:5432/flexstore
./gradlew :spring-infra:bootRun
```

The server starts on [http://localhost:8080](http://localhost:8080) (health check: `/actuator/health`).

The database can be configured either with:
- `FLEXSTORE_DATABASE_URL`: a connection string `postgres(ql)://user:password@host:port/database` (as given by Render) or a full JDBC URL including the credentials
- or the separate variables `FLEXSTORE_DATASOURCE_HOST`, `FLEXSTORE_DATASOURCE_HOST_SUFFIX`, `FLEXSTORE_DATASOURCE_PORT`, `FLEXSTORE_DATASOURCE_DATABASE`, `FLEXSTORE_DATASOURCE_USERNAME`, `FLEXSTORE_DATASOURCE_PASSWORD` (see `backend/spring-infra/.env`)

### 3. Front end

**Prerequisites**: Node 20 or later (the CI uses Node 22).

Add a `front/.env` file:

```bash
VITE_API_URL=http://localhost:8080
```

Then:

```bash
cd front
npm ci
npm run dev
```

The shop starts on [http://localhost:5173](http://localhost:5173).

### Demo account

The migrations create an administration account: **`admin@flexstore.com` / `admin`**. Change its password on any public deployment.

---

## 🔌 API

The OpenAPI spec is in [`backend/spring-infra/src/main/resources/api/openapi.yaml`](backend/spring-infra/src/main/resources/api/openapi.yaml). The front end's TypeScript client (`front/src/generated`) is generated from it:

```bash
cd front
npm run gen:api
```

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/products/` | All products |
| `GET` | `/api/products/{id}` | One product (`404` if it does not exist) |
| `POST` | `/api/products` | Create a product |
| `GET` | `/api/cart/cart-items` | Cart items and total |
| `POST` | `/api/cart/save` | Add or update a cart item (quantity `0` removes it) |
| `POST` | `/api/users/login` | Sign in (`401` on wrong credentials) |
| `GET` | `/api/users` | All users |
| `GET` | `/api/users/{id}` | One user |
| `POST` | `/api/users` | Create a user (`409` if the email is already used) |
| `PUT` | `/api/users/{id}` | Update a user (without password: the current one is kept) |
| `DELETE` | `/api/users/{id}` | Delete a user |

Passwords are hashed with bcrypt and never returned by the API.

---

## ✅ Testing

**Back end** (from `backend/`):

```bash
./gradlew test             # unit tests, at the use case level (mocked ports)
./gradlew integrationTest  # *IT tests: Spring Boot + PostgreSQL
```

Integration tests use the PostgreSQL database on `localhost:5432` (`flexstore` / `flex` / `flex`, see `application-test.yml`): start it first with `docker compose up -d db`.

**Front end** (from `front/`):

```bash
npm test        # Vitest
npm run lint    # ESLint
npm run build   # type check (vue-tsc) + production build
```

---

## 🚢 CI and deployment

- **CI** (`.github/workflows/ci.yml`): on every pull request and push to `main`, back-end unit and integration tests (with a PostgreSQL service) and front-end build and tests
- **Front end**: deployed to Cloudflare Pages on every push to `main` that touches `front/` (`.github/workflows/vue-pages.yml`)
- **Back end**: deployed to Render from `main` with the root `Dockerfile` (`render.yaml`); the database is given to the app as `FLEXSTORE_DATABASE_URL`

---

## 📄 License

MIT — see LICENSE
