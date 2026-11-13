# flexstore

> **Use-Case First** e-commerce demo: a **Kotlin** back end with a clean hexagonal architecture (Domain ↔ Application ↔ Infrastructure) and Spring Boot adapters, and a **Vue 3** shop front end.

Live demo: https://flexstore.tech (deployed on a VPS).

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
- Catalog: product cards, category filter (clothing, electronics, accessories), sort by name or price; the products are translated into French and English
- Product detail: description, quantity, add to cart, save to the wishlist, products of the same category
- Reviews and ratings: a signed-in customer who bought and paid for a product rates it from 1 to 5 stars and comments it, once per product (the form is offered to them only; the others are told why); the reviews (named by the first name and the initial of the last name) and the average rating are shown on the product page, the average rating on the catalog cards; an administrator deletes a review from the product page, which the rating no longer counts and which the customer may write again
- Cart: quantity per line, remove, total, item count badge in the header; one cart per visitor and per customer, merged at sign-in
- Product images: the administrators give the image of a product (JPEG, PNG or WebP, 2 MB at most), kept in the database resized as JPEG in two formats, large (1600 px, 500 KB at most) for the page of the product and small (400 px, 60 KB at most) for the catalog and the cart; the category icon stays for the products without image
- Stock: the number of units of each product is shown in the catalog (out of stock, a few left); an order asking for more than the stock is refused; the units of an order are reserved until it is paid, and go back to the stock when it is cancelled or refunded; the administrators give the stock of each product and its alert threshold (5 by default), and see the products whose stock is at or under it
- Public pages in French and English (`/fr/...`, `/en/...`, each linking to its other version), pre-rendered for the search engines; the legal notice, the terms of sale and the privacy policy are written by the administrators (see Administration); the accounts of the shop on the social networks (Facebook, Instagram, X, TikTok, YouTube, Twitch) are shown when the seller gave them

**Orders** (sign-in required)
- Checkout page: the customer chooses the delivery address and the billing address (the same by default) among their addresses or enters another one (with its recipient, the customer by default, maybe a company), which is added to their addresses; they choose the shipping method (standard 4.90 €, express 9.90 €; the standard shipping is free from a threshold of items, 50 € by default), may enter a discount code and must accept the current version of the terms of sale (if they are published again meanwhile, the customer reads them again)
- Discount codes: an administrator gives a code (3 to 30 upper-case letters and digits) that takes a percentage or an amount off the items of an order, with an expiry date and a usage limit; the checkout previews what the code takes off; the orders that used a code keep their discount, even when the code is deleted
- Place an order from the cart: the order refers to both addresses, keeps the name and the price of the products, the shipping, the discount and the accepted terms of sale, and awaits its payment
- Pay an order with Stripe Checkout (the card is entered on the page of Stripe, never on the shop), or with a simulated payment without a Stripe key; when the customer comes back from Stripe, the shop asks Stripe whether the order was paid, in case its notification did not arrive; once paid, the cart is emptied, the invoice is issued and a confirmation email is sent to the customer (can be turned off in the administration settings)
- Unpaid orders: an order not paid within the payment delay (10 minutes by default, set in the settings) is cancelled by a task that runs every minute, and its units go back to the stock
- Abandoned cart reminder: when an administrator turns it on in the settings (off by default), a customer who left items in their cart for the chosen delay (1 hour to 30 days, 24 hours by default) gets one email that lists them and leads back to the cart; the cart is reminded again only after it changes and is left again. A visitor has no email and is never reminded; the carts that exist when the reminder is introduced are not reminded until their customer changes them. A task checks the carts every 5 minutes
- System user: the scheduled tasks (unpaid orders, abandoned cart reminder) run as a `system` user, created by the migrations with the `SYSTEM` role. Nobody can sign in with it (`401`, the same answer as a wrong password), and an administrator can neither create a user with the `SYSTEM` role (`403`) nor edit the system user; the users list of the administrator shows it with the label "System" (translated) and offers no edit button for it
- Orders page: the customer's orders, newest first, with their status (awaiting payment, paid, processing, shipped, delivered, cancelled, refunded) and total
- Pages: the orders of the customer, the orders of all the customers and the users (administration) are read a page at a time (`GET /api/orders`, `GET /api/admin/orders`, `GET /api/users`, with the query parameters `page`, from 0, and `size`, 20 by default and 100 at most: a larger size is brought back to 100); the answer stays an array and gives the number of items of the whole list in the header `X-Total-Count`. The filters of the administration orders apply to the orders of the page shown. The catalog and the other administration lists are not paginated
- Order detail: date, status, lines (product, unit price, quantity, total), shipping, discount and order total; shown right after the order is placed
- Order tracking: when an administrator ships an order (the next status of a processed order), they can give the carrier (Colissimo, Chronopost, UPS, DHL, FedEx or another) and the tracking number, together or not at all; the customer sees them on the order detail, with a link to the tracking page of the carrier when it has one, and in the email that tells them the order is shipped; an administrator can correct them while the order is shipped or delivered
- Invoices and credit notes: a paid order is invoiced once, with the next number of the sequence and the seller of the shop; the customer downloads the invoice (PDF) from the order detail; when an administrator refunds an order, a credit note that cancels the invoice is issued and downloaded the same way
- Refund: an administrator cancels a paid or processing order (not yet shipped): the payment is refunded (Stripe), the units go back to the stock, the credit note is issued and the customer is told by email

**Account** (sign-in required)
- Create an account in express mode (first name, last name, email, password) or with more details (phone number, postal address, which can also be given later; a customer who buys for their company ticks *I am a company* and gives its name, its SIREN and, optionally, its intra-community VAT number, which must match the SIREN for a French company: the address is then sent to the company, for their attention, and the invoices carry its name and VAT number): the account waits for the verification of the email (`PENDING_VERIFICATION`), the customer is signed in at once and can order
- Verify the email with the link received by email (valid for 1 hour): the account becomes `ACTIVE`; a new link can be asked for from the profile
- Sign in with the password or with Google, sign out, profile, change password; forgotten password: a link sent by email (valid for 1 hour, usable once) lets the customer choose a new one, and the answer is the same whether the email has an account or not
- Two-factor authentication (TOTP, with an authenticator app): a customer or an administrator turns it on from their profile (QR code, then the first code to confirm) and off with their password; once on, the sign-in asks for the code after the password; an administrator can turn it off for another account that lost its device
- Sessions and brute-force protection: the session is renewed with a refresh token (see Authentication); after 5 failed sign-ins in a row (a wrong password or a wrong code for an existing account), the account is locked (`LOCKED`) until an administrator reactivates it; a successful sign-in starts the count again; a wrong password and an unknown email still get the same answer
- Cookies: a banner asks the visitor at their first visit to accept or refuse the preference cookies (the light or dark mode and the language kept for the next visits); the essential cookies (sign-in, cart) need no consent; the customer changes their choice in their account (profile page), and refusing forgets the stored preferences. A visitor keeps their choice in the browser (`localStorage`); a signed-in customer has it in the database (`user_setting`, `GET` and `PUT /api/cookie-consent`) for 6 months, after which they are asked again; at sign-in, the choice of the browser is recorded in their account and removed from the browser
- My preferences: the display mode (light or dark) and the language of a signed-in customer are kept in their account (`/api/color-mode`, `/api/language`) and found again on every device
- My addresses: see, add, edit and remove the postal addresses of the account; the orders keep the address they were sent to
- Wishlist: a customer saves products to find them again later (once per product, newest first)
- Loyalty: one point for each whole euro or dollar of the paid orders of the customer, shown on their account

**Administration** (`ADMIN` role)
- Sales dashboard: revenue, number of orders and average basket, orders per day (bars) and the 5 best sellers, over the last 7, 30 or 90 days; only the paid orders count (not the awaiting, cancelled or refunded ones), on the day they were placed in Paris time
- Users: list with their role, create with a role, edit first name/last name/email/role, change the status of the account (active, suspended, locked, disabled), delete, turn off their two-factor authentication
- Products: list, create (a free SKU is proposed from the category and the name), edit name/description/category/price in each language, and a promotion: a percentage off the price, the regular price shown struck through; images and stocks (see Shop)
- Orders: list the orders of all the customers, move each one to its next status (paid, processing, shipped, delivered; the payment moves an order awaiting it), give or correct the tracking, refund, download the invoice and the credit note of any order
- Discount codes: give and delete (see Orders)
- Seller: the company that sells and is written on the invoices (name, address, SIRET, SIREN, VAT number, legal form, share capital, registry, contact, social networks); a setup assistant asks for it until the shop is set up
- Legal pages: write the legal notice, the terms of sale and the privacy policy; each publication is a new version, the ones before are kept
- Shop page content: the key words and the description of the pages for the search engines, and the Google Analytics id
- Settings: order confirmation email on/off, payment delay, free shipping threshold, abandoned cart reminder on/off and its delay
- Environment: which environment variables of the back end are defined: the plain ones with their value, the passwords, keys and secrets only as defined or not, never with their value

---

## 🧱 Architecture (Hexagonal)

**Domain**: pure Kotlin (entities, value objects, use cases, ports, domain errors). No framework.\
**Infrastructure**: Spring Boot adapters implementing ports (REST controllers as *input adapters*, JPA as *output adapter*).

Users, products and carts are stored in PostgreSQL (JPA adapters in `spring-infra/.../adapter/persistence/jpa`: `entity` and `repository`). A cart item only keeps the product id and the quantity: the product (name, price) is always read from the catalog.

**Carts**: each cart has an owner.
- A **visitor** gets a cart id (a UUID) with their first item (`POST /api/cart/save` returns it); the front end keeps it in `localStorage` and sends it in the `X-Cart-Id` header. No server session is needed.
- A **signed-in customer** has one cart, found with the token, on every device.
- At **sign-in**, the front end calls `POST /api/cart/merge`: the visitor cart is merged into the customer cart (all the items are kept; for a product in both carts, the larger quantity is kept), then deleted.

The cart rules live in the `Cart` aggregate (domain); the use cases (`AddItemToCartUseCase`, `ChangeCartItemQuantityUseCase`, `MergeCartsUseCase`...) load and save it through the `CartRepository` port.

---

## 📦 Modules

```
backend/
  domain/         # Entities, value objects, use cases, ports, errors
  infra/          # Infrastructure not linked to a framework (in-memory repositories; only the two-factor challenges one is used by the app)
  spring-infra/   # Spring adapters (REST controllers, JPA), OpenAPI spec
  migrations/     # Flyway SQL migrations
  mcp/            # MCP server that exposes the products to AI assistants (calls the use cases of the domain)
front/            # Vue 3 + Vuetify + Pinia + vue-i18n shop
docs/
  database.md     # Physical data model of the database (Mermaid diagram)
```

The tables, their keys and their relations are drawn in [docs/database.md](docs/database.md), updated with every migration.

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

**Emails**: the order confirmation emails are sent with the SMTP server configured in `application.yml` (`spring.mail.*`), through these variables; when `FLEXSTORE_MAIL_HOST` is empty, they are written to the logs instead.

| Variable | Default | |
|---|---|---|
| `FLEXSTORE_MAIL_HOST` | empty (logs) | SMTP server, e.g. `smtp-relay.brevo.com` |
| `FLEXSTORE_MAIL_PORT` | `587` | |
| `FLEXSTORE_MAIL_USERNAME` / `FLEXSTORE_MAIL_PASSWORD` | empty | credentials given by the provider |
| `FLEXSTORE_MAIL_SMTP_AUTH` / `FLEXSTORE_MAIL_SMTP_STARTTLS` | `true` / `true` | `false` / `false` for a local server such as Mailpit |
| `FLEXSTORE_MAIL_FROM` | `Flexstore <no-reply@flexstore.com>` | sender (often to be validated at the provider) |
| `FLEXSTORE_FRONT_URL` | `http://localhost:5173` | address of the shop, for the link to the order and the return from the payment |
| `FLEXSTORE_GOOGLE_CLIENT_ID` | empty | OAuth client id of the shop (Google Cloud console), turns on Sign in with Google; empty: Google tokens are refused |
| `FLEXSTORE_MCP_ENABLED` | `true` | `false` turns the MCP server (`POST /mcp`) off |
| `FLEXSTORE_STRIPE_SECRET_KEY` | empty | secret key of Stripe (`sk_test_...`); empty: simulated payments, confirmed at once |
| `FLEXSTORE_STRIPE_WEBHOOK_SECRET` | empty | secret of the webhook endpoint (`whsec_...`), to verify the notifications of Stripe |

Locally, `docker compose up -d mailpit` starts an SMTP server that catches the emails ([http://localhost:8025](http://localhost:8025)): start the back end with `FLEXSTORE_MAIL_HOST=localhost FLEXSTORE_MAIL_PORT=1025 FLEXSTORE_MAIL_SMTP_AUTH=false FLEXSTORE_MAIL_SMTP_STARTTLS=false` (the `app` service of docker compose is already configured so). Sending can be turned off in Administration > Paramètres.

To pay with Stripe (test mode): create a Stripe account, copy its test secret key (`sk_test_...`), and add a webhook endpoint `https://<back end>/api/payments/stripe/webhook` for the event `checkout.session.completed`, whose signing secret (`whsec_...`) goes in `FLEXSTORE_STRIPE_WEBHOOK_SECRET`. Locally, `stripe listen --forward-to localhost:8080/api/payments/stripe/webhook` gives a signing secret and forwards the notifications. The test card `4242 4242 4242 4242` pays with any future date and any code.

**Sign in with Google**: in the [Google Cloud console](https://console.cloud.google.com/apis/credentials), create an OAuth client id of type *Web application*, add the address of the front end (`http://localhost:5173`, and the public address) to its *Authorized JavaScript origins*, then give the same client id to the back end (`FLEXSTORE_GOOGLE_CLIENT_ID`) and to the front end (`VITE_GOOGLE_CLIENT_ID`). On the VPS, both come from one GitHub Actions *variable* (not a secret), `FLEXSTORE_GOOGLE_CLIENT_ID`: the deployment writes it in the `.env` of the server and builds the front end with it (see `docs/deployment-vps.md`). The button is only shown when the front end has the client id. The back end verifies the ID token of Google (`POST /api/users/login/google`), signs in the account that has the same email (a customer who has none gets an active account, Google having verified the email) and gives the same session as the password login (an administrator with two-factor authentication still gives the code).

Set `FLEXSTORE_JWT_SECRET` (at least 32 characters) to sign the authentication tokens. Without it, a random secret is used and tokens stop working when the back end restarts.

The database can be configured either with:
- `FLEXSTORE_DATABASE_URL`: a connection string `postgres(ql)://user:password@host:port/database` (as given by a managed PostgreSQL) or a full JDBC URL including the credentials
- or the separate variables `FLEXSTORE_DATASOURCE_HOST`, `FLEXSTORE_DATASOURCE_HOST_SUFFIX`, `FLEXSTORE_DATASOURCE_PORT`, `FLEXSTORE_DATASOURCE_DATABASE`, `FLEXSTORE_DATASOURCE_USERNAME`, `FLEXSTORE_DATASOURCE_PASSWORD` (see `backend/spring-infra/.env`)

The back end can also run in Docker, with the root `Dockerfile` (unit tests included):

```bash
docker compose up -d --build app
```

### 3. Front end

**Prerequisites**: Node 22, given in `front/.nvmrc` (`nvm use` in `front/`) and used by the CI and the deployment. Vite 7 needs Node 20.19 or later: `npm install` refuses an older version, and with Node 18 the tests fail with `crypto.hash is not a function`. The CI and the deployment use npm 12.2.2 (`npm install -g npm@12.2.2`), which needs Node 22.22.2 or later.

Add a `front/.env` file:

```bash
VITE_API_URL=http://localhost:8080
VITE_SITE_URL=http://localhost:5173
# Optional: OAuth client id of Google, shows the "Sign in with Google" button (same value as FLEXSTORE_GOOGLE_CLIENT_ID)
VITE_GOOGLE_CLIENT_ID=
```

`npm run build` pre-renders the public pages (the routes that say `indexed` in `front/src/routes.ts`: home, catalog, products, about, legal pages) as static HTML, so that search engines read them without running JavaScript. It reads the products from `VITE_API_URL` at build time; when the API does not answer, the product pages are left out and the build goes on.

The public pages exist in French (`/fr/a-propos`) and in English (`/en/about`): the words of the address follow the language, without accent (`/fr/produits/vetements`, `/en/products/clothing`): the language of such a page is the one of its address (the root and the addresses without prefix lead to the French pages, see `front/public/_redirects`), the links of an English page lead to English pages, and each page links to its other version (`hreflang`, in the page and in `sitemap.xml`). The other pages (cart, account, administration) have no prefix and follow the language chosen in the menu.

`VITE_SITE_URL` is the public address of the shop: the search engines are given it in the canonical address of each page (the address of the page is used when it is missing), and `npm run build` writes `sitemap.xml` (the pre-rendered pages) and adds its address to `robots.txt` only when it is set.

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
| `PUT` | `/api/products/{id}` | `ADMIN` | Edit a product (`404` if it does not exist) |
| `GET` | `/api/cart/cart-items` | public | Items and total of the cart of the customer (token) or of the visitor (`X-Cart-Id`) |
| `POST` | `/api/cart/save` | public | Add or update a cart item (quantity `0` removes it); returns the visitor cart id |
| `POST` | `/api/cart/merge` | signed in | Merge the visitor cart (`X-Cart-Id`) into the customer cart |
| `POST` | `/api/orders` | signed in | Place an order from the cart of the customer, with `{ deliveryAddress, billingAddress? }` (the delivery address when no billing address); the order awaits its payment, the cart is kept (`400` incomplete address, `422` empty cart) |
| `POST` | `/api/orders/{id}/payment` | signed in | Start the payment of an order: `{ checkoutUrl, outcome }`, the page to open (`PAID` at once when simulated); once paid, the cart is emptied and the order confirmed by email (`409` already paid) |
| `POST` | `/api/payments/stripe/webhook` | Stripe signature | Notification of Stripe (`checkout.session.completed`): the order is paid (`404` without Stripe key) |
| `GET` | `/api/orders` | signed in | Orders of the customer, newest first |
| `GET` | `/api/orders/{id}` | signed in | One order of the customer (`404` for the order of another customer) |
| `GET` | `/api/product-images` | public | The ids of the products that have an image |
| `GET` | `/api/products/{id}/image?format=small\|large` | public | The image of a product as JPEG, with an ETag (`304` when unchanged, `404` without image) |
| `PUT` / `DELETE` | `/api/admin/products/{id}/image` | `ADMIN` | Give (multipart `image`, 2 MB at most, `413` larger, `415` not an image) or remove the image of a product |
| `GET` | `/api/stocks`, `/api/stocks/{productId}` | public | The stocks of the products (`0` for a product without stock) |
| `PUT` | `/api/admin/stocks/{productId}` | `ADMIN` | Give the stock of a product, `{ quantity }` (`400` negative, `404` unknown product) |
| `GET` | `/api/admin/settings` | `ADMIN` | Settings of the shop (order confirmation email, abandoned cart reminder and delay, payment delay, free shipping threshold, seller setup) |
| `POST` | `/api/admin/settings/order-confirmation-email/enable` | `ADMIN` | Send a confirmation email when an order is placed |
| `POST` | `/api/admin/settings/order-confirmation-email/disable` | `ADMIN` | Stop sending the order confirmation email |
| `POST` | `/api/admin/settings/abandoned-cart-reminder/enable`, `.../disable` | `ADMIN` | Turn the abandoned cart reminder on or off |
| `PUT` | `/api/admin/settings/abandoned-cart-delay`, `/payment-delay`, `/free-shipping-threshold` | `ADMIN` | Change the delay before a reminder (1 hour to 30 days), the payment delay (1 minute to 1 day), the free shipping threshold |
| `GET` | `/api/admin/environment` | `ADMIN` | The environment variables of the shop with their `sensitivity` (`PLAIN` / `SECRET`), their `necessity` (`REQUIRED` / `OPTIONAL`) and their `status` (`DEFINED` / `UNDEFINED`: defined when neither null, empty nor blank); the `value` is given for a defined `PLAIN` variable only, never for a `SECRET` |
| `POST` | `/api/accounts` | public | Create the account of a customer, status `PENDING_VERIFICATION` and send a verification link valid for 1 hour (`409` if the email is used, `422` if a field is missing or the password is too short) |
| `GET` | `/api/accounts/me` | signed in | The account of the signed-in user, with their phone number and postal addresses |
| `GET` | `/api/accounts/me/addresses` | signed in | The postal addresses of the signed-in user, in their order |
| `PUT` | `/api/accounts/me/addresses/{position}` | signed in | Change an address (`0` for the first one); the orders keep theirs (`400` incomplete, `404` no address there) |
| `DELETE` | `/api/accounts/me/addresses/{position}` | signed in | Remove an address; the orders keep theirs (`404` no address there) |
| `POST` | `/api/accounts/email-verification` | public | Verify the email with the token of the link: the account becomes `ACTIVE` (`404` unknown or used link, `410` link older than 1 hour) |
| `POST` | `/api/accounts/me/verification-email` | signed in | Send a new verification link; the previous ones stop working (`409` if already verified) |
| `POST` | `/api/users/login/google` | public | Sign in with a Google account: takes `{ idToken }`, answers like the login (`401` when Google's token is refused or Sign in with Google is off) |
| `POST` | `/api/users/login` | public | Sign in: returns a token (15 minutes), a refresh token (7 days) and the user (`401` on wrong credentials) |
| `POST` | `/api/users/refresh` | public | Renew the session with a refresh token: returns a new token and a new refresh token (`401` unknown, expired or already used refresh token, `403` suspended, locked or disabled account) |
| `POST` | `/api/users/logout` | public | Sign out: ends the session of a refresh token (`204`, also when it is unknown or already ended) |
| `PUT` | `/api/users/{id}/status` | `ADMIN` | Change the status of another account: `ACTIVE`, `SUSPENDED`, `LOCKED` or `DISABLED`; a suspended, locked or disabled account can neither sign in nor order (`403` with its `status`) |
| `POST` | `/api/users/{id}/password` | the user `{id}` | Change one's own password (`400` if the current one is wrong) |
| `GET` | `/api/users` | `ADMIN` | All users |
| `GET` | `/api/users/{id}` | `ADMIN` | One user |
| `POST` | `/api/users` | `ADMIN` | Create a user, with its `role` (`USER` by default, or `ADMIN`); `409` if the email is already used |
| `PUT` | `/api/users/{id}` | `ADMIN` | Edit a user: first name, last name, email and role (`409` if the email is used by another user); the password is not changed by an edit |
| `DELETE` | `/api/users/{id}` | `ADMIN` | Delete a user |
| `POST` | `/api/accounts/me/addresses` | signed in | Add an address at the end of the addresses |
| `POST` | `/api/password-reset-requests` | public | Send a link to choose a new password (valid for 1 hour); the same answer whether the email has an account or not |
| `POST` | `/api/password-resets` | public | Choose a new password with the token of the link (usable once) |
| `POST` | `/api/users/login/two-factor-authentication` | public | Complete a sign-in challenged for its second factor with the code of the authenticator app |
| `POST` | `/api/users/{id}/two-factor-authentication/enrollment` | the user `{id}` | Start the enrollment: a pending secret and the link of its QR code |
| `POST` | `/api/users/{id}/two-factor-authentication/confirmation` | the user `{id}` | Confirm the enrollment with the first code: two-factor authentication is on |
| `DELETE` | `/api/users/{id}/two-factor-authentication` | the user `{id}` | Turn it off with the current password |
| `PUT` | `/api/users/{id}/two-factor-authentication` | `ADMIN` | Turn it off for another account, without a password |
| `GET` / `PUT` | `/api/color-mode`, `/api/language` | signed in | The display mode and the preferred language of the customer (`NONE` when never chosen) |
| `GET` / `PUT` | `/api/cookie-consent` | signed in | The cookie consent of the customer, kept 6 months |
| `GET` | `/api/admin/products/{id}` | `ADMIN` | A product with its translations in every language, to edit it |
| `GET` | `/api/admin/products/sku-proposal` | `ADMIN` | A SKU free in the catalog, from the category and the name |
| `GET` / `POST` | `/api/products/{id}/reviews` | public / signed in | The reviews of a product / add one (a customer who paid for the product, once) |
| `GET` | `/api/products/{id}/review-eligibility` | signed in | Whether the customer may review the product, and why not |
| `GET` | `/api/product-ratings` | public | The average rating of each reviewed product |
| `DELETE` | `/api/admin/reviews/{id}` | `ADMIN` | Delete a review |
| `GET` | `/api/wishlist` | signed in | The products saved to the wishlist, newest first |
| `PUT` / `DELETE` | `/api/wishlist/{productId}` | signed in | Save a product to the wishlist / remove it |
| `GET` | `/api/loyalty` | signed in | The loyalty points of the customer |
| `GET` | `/api/orders/shipping-options` | signed in | The shipping methods with their cost for the cart |
| `GET` | `/api/orders/discount-preview` | signed in | What a discount code takes off the cart (`404` unknown code, `410` expired or used up) |
| `POST` | `/api/orders/{id}/payment/verification` | signed in | Ask the payment provider whether the order was paid, and pay it here if so |
| `GET` | `/api/orders/{id}/invoice`, `/api/orders/{id}/credit-note` | signed in | The invoice of a paid order, the credit note of a refunded one (PDF) |
| `GET` | `/api/admin/orders`, `/api/admin/orders/{id}` | `ADMIN` | The orders of all the customers (paginated), one order |
| `POST` | `/api/admin/orders/{id}/status/advance` | `ADMIN` | Move an order to its next status, with the carrier and tracking number when shipping it |
| `PUT` | `/api/admin/orders/{id}/tracking` | `ADMIN` | Correct the tracking of a shipped or delivered order |
| `POST` | `/api/admin/orders/{id}/refund` | `ADMIN` | Cancel a paid order not yet shipped, refund its payment, put its units back in stock, issue its credit note (`409` not refundable, `502` the provider refused) |
| `GET` | `/api/admin/orders/{id}/invoice`, `/api/admin/orders/{id}/credit-note` | `ADMIN` | The invoice and the credit note of any order (PDF) |
| `GET` / `POST` | `/api/admin/discount-codes` | `ADMIN` | List / give a discount code (`400` invalid, `409` already given) |
| `DELETE` | `/api/admin/discount-codes/{code}` | `ADMIN` | Delete a discount code |
| `GET` | `/api/admin/stocks/low` | `ADMIN` | The stocks at or under the alert threshold of their product |
| `GET` / `PUT` | `/api/admin/stocks/{productId}/alert` | `ADMIN` | The alert threshold of a product (5 by default) / change it |
| `GET` / `PUT` | `/api/admin/seller` | `ADMIN` | The seller written on the next invoices / change it |
| `PUT` | `/api/admin/seller/setup` | `ADMIN` | Finish the setup assistant: save the seller and turn the seller setup on |
| `GET` | `/api/social-networks` | public | The accounts of the shop on the social networks |
| `GET` | `/api/legal-pages/{kind}` | public | The current version of a legal page (`legal-notice`, `terms-of-sale`, `privacy-policy`) |
| `PUT` | `/api/admin/legal-pages/{kind}` | `ADMIN` | Publish a new version of a legal page |
| `GET` | `/api/shop-page-contents` | public | The key words, the description and the Google Analytics id |
| `PUT` | `/api/admin/shop-page-contents/{type}` | `ADMIN` | Write one of these contents |
| `GET` | `/api/admin/sales-dashboard` | `ADMIN` | Revenue, orders, average basket, orders per day and best sellers |

Passwords are hashed with bcrypt and never returned by the API.

### Authentication

The API uses **JWT bearer tokens** (Spring Security, OAuth2 resource server): the access tokens are stateless, the
sessions that renew them are kept by the back end (refresh tokens):

1. `POST /api/users/login` returns `{ token, expiresAt, refreshToken, refreshExpiresAt, user }`; the token is signed with HMAC-SHA256 (`FLEXSTORE_JWT_SECRET`), contains the user id (`sub`) and roles (`roles`) and is valid for 15 minutes (`flexstore.security.jwt-ttl`)
2. Protected endpoints require the header `Authorization: Bearer <token>`: `401` without a valid token, `403` without the required role
3. The front end keeps the session (token, refresh token, user, expiry) in the `sessionStorage` of the tab: reloading the page keeps the user signed in, closing the tab or the expiry of the refresh token (7 days) signs out. When the API rejects the token (`401`), the front end renews it once with the refresh token and sends the request again (the requests rejected together renew it once); when the session cannot be renewed, the user is signed out and sent back to the login page. Signing out also ends the session on the back end (`POST /api/users/logout`)
4. **Refresh tokens** are random opaque secrets, one per session (`user_token` table, type `REFRESH_SESSION`, only their SHA-256 hash is stored). A refresh token works once: `POST /api/users/refresh` revokes it and returns the next one. When a refresh token that was already used (or whose session was ended) comes back, it was replayed or stolen: **all the sessions of its user are ended**. A refresh is refused when the account is suspended, locked or disabled (`403`) or deleted (`401`); the sessions of a deleted user are deleted with them. A token already issued still works until it expires (15 minutes)

Users have a role, `USER` (default) or `ADMIN`, chosen by the administrator who creates or edits them; it is stored in the database. Creating and editing users is checked by the domain: the creator or editor must be an existing administrator (`403` otherwise).

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/users/login -H 'Content-Type: application/json' \
  -d '{"email": "admin@flexstore.com", "password": "admin"}' | jq -r .token)
curl localhost:8080/api/users -H "Authorization: Bearer $TOKEN"
```

---

## 🤖 MCP server (products)

The `mcp` module is a [Model Context Protocol](https://modelcontextprotocol.io) server, so that an AI assistant
(Claude Desktop, Claude Code...) can read the products of the shop. It runs inside the back end: the tools call the
use cases of the domain (`ProductUseCases`) and are described with the types of the
[MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk), and `spring-infra` serves them at `POST /mcp` with
the stateless Streamable HTTP transport of the SDK (public like the catalog: it only gives the products).

| Tool | Arguments | Answers |
|---|---|---|
| `list_products` | `language` (optional: `EN` or `FR`, default `EN`) | All the products: id, SKU, name, description, category, price, currency, promotion percentage (while it runs) and current price |
| `get_product` | `id`, `language` (optional) | One product, or an error when it does not exist |

It is on by default; `FLEXSTORE_MCP_ENABLED=false` (property `flexstore.mcp.enabled`) turns it off.

```bash
claude mcp add --transport http flexstore http://localhost:8080/mcp
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
npm run build   # type check (vue-tsc) + production build, with the public pages pre-rendered (vite-ssg)
```

---

## 🚢 CI and deployment

- **CI** (`.github/workflows/ci.yml`): on every pull request and push to `main`, back-end unit and integration tests (with a PostgreSQL service) and front-end lint, build and tests
- **VPS** (https://flexstore.tech): every merge on `main` whose CI is green deploys the shop to a Debian VPS: back end image pushed to `ghcr.io`, PostgreSQL, back end and Caddy (HTTPS) in Docker Compose, the pre-rendered front end copied over SSH (`.github/workflows/deploy.yml`, `deploy/`); the set up of the server and of the GitHub secrets is in [docs/deployment-vps.md](docs/deployment-vps.md)
- **Cloudflare Pages and Render: no longer used.** The workflow `.github/workflows/vue-pages.yml` (Cloudflare Pages) is kept for reference but disabled: it only starts by hand and its job is skipped. The Render blueprint (`render.yaml`) is removed. `front/public/_headers` is specific to Cloudflare Pages: Caddy sets the HTTP headers on the VPS

---

## 📄 License

MIT — see LICENSE
