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
- Cart: quantity per line, remove, total, item count badge in the header; one cart per visitor and per customer, merged at sign-in

**Orders** (sign-in required)
- Place an order from the cart: the order keeps the name and the price of the products, the cart is emptied, and a confirmation email is sent to the customer (can be turned off in the administration settings)
- Orders page: the customer's orders, newest first, with their status and total
- Order detail: date, status, lines (product, unit price, quantity, total) and order total; shown right after the order is placed

**Account and administration** (sign-in required)
- Sign in, profile, change password
- Administration (`ADMIN` role): users (list with their role, create with a role, edit name/email/role, delete), products (create), settings (order confirmation email on/off)

---

## 🧱 Architecture (Hexagonal)

**Domain**: pure Kotlin (entities, value objects, use cases, ports, domain errors). No framework.\
**Infrastructure**: Spring Boot adapters implementing ports (REST controllers as *input adapters*, JPA as *output adapter*).

Users, products and carts are stored in PostgreSQL (JPA adapters in `spring-infra/.../adapter/jpa`). A cart item only keeps the product id and the quantity: the product (name, price) is always read from the catalog.

**Carts**: each cart has an owner.
- A **visitor** gets a cart id (a UUID) with their first item (`POST /api/cart/save` returns it); the front end keeps it in `localStorage` and sends it in the `X-Cart-Id` header. No server session is needed.
- A **signed-in customer** has one cart, found with the token, on every device.
- At **sign-in**, the front end calls `POST /api/cart/merge`: the visitor cart is merged into the customer cart (all the items are kept; for a product in both carts, the larger quantity is kept), then deleted.

The cart rules live in the `Cart` aggregate (domain); the use cases (`AddItemToCartUseCase`, `ChangeCartItemQuantityUseCase`, `MergeCartsUseCase`...) load and save it through the `CartRepository` port.

---

## 📦 Modules

```
backend/
  app/            # not used at the moment
  domain/         # Entities, value objects, use cases, ports, errors
  infra/          # Infrastructure not linked to a framework (in-memory repositories, not used by the app)
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

Flyway migrations run when the back end starts; they create the catalog (9 products). To open a SQL console:

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

**Emails**: the order confirmation emails are sent with SMTP when `SPRING_MAIL_HOST` is set (with `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, sender `FLEXSTORE_MAIL_FROM`, shop address for the links `FLEXSTORE_FRONT_URL`); without it, they are written to the logs. Locally, `docker compose up -d mailpit` starts an SMTP server that catches them: start the back end with `SPRING_MAIL_HOST=localhost SPRING_MAIL_PORT=1025` and read them on [http://localhost:8025](http://localhost:8025).

Set `FLEXSTORE_JWT_SECRET` (at least 32 characters) to sign the authentication tokens. Without it, a random secret is used and tokens stop working when the back end restarts.

The database can be configured either with:
- `FLEXSTORE_DATABASE_URL`: a connection string `postgres(ql)://user:password@host:port/database` (as given by Render) or a full JDBC URL including the credentials
- or the separate variables `FLEXSTORE_DATASOURCE_HOST`, `FLEXSTORE_DATASOURCE_HOST_SUFFIX`, `FLEXSTORE_DATASOURCE_PORT`, `FLEXSTORE_DATASOURCE_DATABASE`, `FLEXSTORE_DATASOURCE_USERNAME`, `FLEXSTORE_DATASOURCE_PASSWORD` (see `backend/spring-infra/.env`)

The back end can also run in Docker, with the root `Dockerfile` (unit tests included):

```bash
docker compose up -d --build app
```

### 3. Front end

**Prerequisites**: Node 20.19 or later, required by Vite 7 (the CI uses Node 22).

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

| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/products/` | public | All products |
| `GET` | `/api/products/{id}` | public | One product (`404` if it does not exist) |
| `POST` | `/api/products` | `ADMIN` | Create a product |
| `GET` | `/api/cart/cart-items` | public | Items and total of the cart of the customer (token) or of the visitor (`X-Cart-Id`) |
| `POST` | `/api/cart/save` | public | Add or update a cart item (quantity `0` removes it); returns the visitor cart id |
| `POST` | `/api/cart/merge` | signed in | Merge the visitor cart (`X-Cart-Id`) into the customer cart |
| `POST` | `/api/orders` | signed in | Place an order from the cart of the customer, then empty the cart (`422` if the cart is empty) |
| `GET` | `/api/orders` | signed in | Orders of the customer, newest first |
| `GET` | `/api/orders/{id}` | signed in | One order of the customer (`404` for the order of another customer) |
| `GET` | `/api/admin/settings` | `ADMIN` | Settings of the shop (`orderConfirmationEmail`: `ENABLED` / `DISABLED`) |
| `POST` | `/api/admin/settings/order-confirmation-email/enable` | `ADMIN` | Send a confirmation email when an order is placed |
| `POST` | `/api/admin/settings/order-confirmation-email/disable` | `ADMIN` | Stop sending the order confirmation email |
| `POST` | `/api/users/login` | public | Sign in: returns a token and the user (`401` on wrong credentials) |
| `POST` | `/api/users/{id}/password` | the user `{id}` | Change one's own password (`400` if the current one is wrong) |
| `GET` | `/api/users` | `ADMIN` | All users |
| `GET` | `/api/users/{id}` | `ADMIN` | One user |
| `POST` | `/api/users` | `ADMIN` | Create a user, with its `role` (`USER` by default, or `ADMIN`); `409` if the email is already used |
| `PUT` | `/api/users/{id}` | `ADMIN` | Edit a user: name, email and role (`409` if the email is used by another user); the password is not changed by an edit |
| `DELETE` | `/api/users/{id}` | `ADMIN` | Delete a user |

Passwords are hashed with bcrypt and never returned by the API.

### Authentication

The API is stateless and uses **JWT bearer tokens** (Spring Security, OAuth2 resource server):

1. `POST /api/users/login` returns `{ token, expiresAt, user }`; the token is signed with HMAC-SHA256 (`FLEXSTORE_JWT_SECRET`), contains the user id (`sub`) and roles (`roles`) and is valid for 1 hour (`flexstore.security.jwt-ttl`)
2. Protected endpoints require the header `Authorization: Bearer <token>`: `401` without a valid token, `403` without the required role
3. The front end keeps the session (token, user, expiry) in the `sessionStorage` of the tab: reloading the page keeps the user signed in, closing the tab or the expiry of the token (1 hour) signs out. When the API rejects the token, the user is signed out and sent back to the login page

Users have a role, `USER` (default) or `ADMIN`, chosen by the administrator who creates or edits them; it is stored in the database. Creating and editing users is checked by the domain: the creator or editor must be an existing administrator (`403` otherwise).

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/users/login -H 'Content-Type: application/json' \
  -d '{"email": "admin@flexstore.com", "password": "admin"}' | jq -r .token)
curl localhost:8080/api/users -H "Authorization: Bearer $TOKEN"
```

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
npm run lint    # ESLint (generated client excluded)
npm run build   # type check (vue-tsc) + production build
```

---

## 🚢 CI and deployment

- **CI** (`.github/workflows/ci.yml`): on every pull request and push to `main`, back-end unit and integration tests (with a PostgreSQL service) and front-end lint, build and tests
- **Front end**: deployed to Cloudflare Pages on every push to `main` that touches `front/` (`.github/workflows/vue-pages.yml`)
- **Back end**: deployed to Render from `main` with the root `Dockerfile` (`render.yaml`); the database is given to the app as `FLEXSTORE_DATABASE_URL`, and `FLEXSTORE_JWT_SECRET` must be set. HTTPS ends at the Render proxy: `server.forward-headers-strategy: framework` makes Spring see the requests as HTTPS

---

## 📄 License

MIT — see LICENSE
