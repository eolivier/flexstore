# Physical data model

The PostgreSQL schema of Flexstore, as created by the Flyway migrations of `backend/migrations` (last migration: **V32**). Update this diagram in the same pull request as any migration that changes the schema (see the `database-convention` skill).

![Physical data model](database.png)

The image is generated from the diagram below by `docs/render-database-diagram.sh`. Each column is written with its name first, then its type, so that the image shows them in this order. The diagram is laid out from left to right by ELK, without crossing links: the relations are written from the table that holds the foreign key towards the table it references when that one should be placed on the right (`address`, `payment`, `product`).

```mermaid
%%{init: {"layout": "elk", "theme": "base", "themeVariables": {"primaryColor": "#DBEAFE", "primaryTextColor": "#111827", "primaryBorderColor": "#1D4ED8", "lineColor": "#374151", "secondaryColor": "#F3F4F6", "tertiaryColor": "#FFFFFF", "attributeBackgroundColorOdd": "#FFFFFF", "attributeBackgroundColorEven": "#F1F5F9"}}}%%
erDiagram
    direction LR

    user {
        id varchar(50) PK
        first_name varchar(255)
        last_name varchar(255)
        email text UK
        password text "bcrypt hash"
        role text "USER or ADMIN"
        status varchar(30) "PENDING_VERIFICATION, ACTIVE, SUSPENDED, LOCKED or DISABLED"
        phone_number varchar(30) "nullable"
        created_at timestamp
        updated_at timestamp
    }

    email_verification {
        id varchar(50) PK
        token_hash varchar(64) UK "SHA-256 of the token of the link"
        user_id varchar(50) FK "a user may have several links over time"
        expires_at timestamptz
        created_at timestamp
        updated_at timestamp
    }

    password_reset {
        id varchar(50) PK
        token_hash varchar(64) UK "SHA-256 of the token of the link"
        user_id varchar(50) FK,UK "only the last link asked"
        expires_at timestamptz
        created_at timestamp
        updated_at timestamp
    }

    user_address {
        id varchar(50) PK
        user_id varchar(50) FK,UK "unique with address_id"
        address_id varchar(50) FK,UK
        position int "order of the addresses of the user"
        created_at timestamp
        updated_at timestamp
    }

    customer_order {
        id varchar(50) PK
        user_id varchar(50) FK
        status varchar(20) "last status: AWAITING_PAYMENT, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED or REFUNDED"
        placed_at timestamptz
        shipping_method varchar(20) "STANDARD or EXPRESS"
        shipping_cost numeric "0 or more, 0 when free"
        discount_code varchar(30) "copy of the code used, no foreign key; nullable: no discount"
        discount_amount numeric "taken off the items, 0 without code"
        delivery_address_id varchar(50) FK
        billing_address_id varchar(50) FK
        payment_id varchar(50) FK "nullable: last payment started"
        created_at timestamp
        updated_at timestamp
    }

    cart {
        id varchar(50) PK
        user_id varchar(50) FK,UK "nullable: the cart of a visitor"
        created_at timestamp
        updated_at timestamp
    }

    address {
        id varchar(50) PK
        recipient_type varchar(20) "PRIVATE_INDIVIDUAL or LEGAL_ENTITY"
        first_name varchar(255) "required for a person, optional contact of a company"
        last_name varchar(255) "required for a person, optional contact of a company"
        company_name varchar(255) "required for a company"
        vat_number varchar(30) "nullable"
        street varchar(255)
        complement varchar(255) "nullable"
        postal_code varchar(20)
        city varchar(100)
        country varchar(100)
        created_at timestamp
        updated_at timestamp
    }

    payment {
        id varchar(50) PK
        provider_reference varchar(255) UK "id of the Stripe Checkout session"
        checkout_url text
        outcome varchar(10) "PENDING or PAID"
        created_at timestamp
        updated_at timestamp
    }

    payment_refund {
        id varchar(50) PK
        payment_id varchar(50) FK,UK "at most one refund per payment"
        provider_reference varchar(255) UK "id of the Stripe refund"
        created_at timestamp
        updated_at timestamp
    }

    customer_order_status_change {
        id varchar(50) PK
        customer_order_id varchar(50) FK,UK "unique with position"
        position int UK "0: placed, then each status reached"
        status varchar(20) "the status reached"
        changed_at timestamptz "when it was reached"
        created_at timestamp
        updated_at timestamp
    }

    invoice {
        id varchar(50) PK
        number varchar(20) UK "year then rank in the year, without gaps: 2026-000042"
        customer_order_id varchar(50) FK,UK "one invoice per paid order"
        issued_at timestamptz "when the order was paid"
        seller_id varchar(50) FK "the version of the seller current at the issue"
        created_at timestamp
        updated_at timestamp
    }

    seller {
        id varchar(50) PK
        version int UK "never changed: each edit adds a version, the highest is the current seller"
        name varchar(255)
        address varchar(500) "on one line"
        siret varchar(20) "14 digits, maybe grouped by spaces"
        vat_number varchar(20) "intra-community: FR12345678901"
        created_at timestamp
        updated_at timestamp
    }

    discount_code {
        id varchar(50) PK
        code varchar(30) UK "upper-case letters and digits"
        reduction_type varchar(10) "PERCENTAGE or AMOUNT"
        reduction_value numeric "1 to 100 (percent), or euros; more than 0"
        expires_at timestamptz
        usage_limit int "1 or more"
        times_used int "0 to usage_limit"
        created_at timestamp
        updated_at timestamp
    }

    invoice_counter {
        id varchar(50) PK
        year int UK "year of issue"
        last_rank int "last rank given in the year, incremented with each invoice"
        created_at timestamp
        updated_at timestamp
    }

    customer_order_line {
        id varchar(50) PK
        customer_order_id varchar(50) FK,UK "unique with position"
        position int UK
        product_id varchar(50) "copy, no foreign key: the order history does not depend on the catalog"
        product_name text "copy at the time of the order"
        unit_price numeric "copy at the time of the order"
        currency varchar(3)
        vat_rate varchar(20) "copy at the time of the order"
        quantity int "greater than 0"
        created_at timestamp
        updated_at timestamp
    }

    product_image {
        id varchar(50) PK
        product_id varchar(50) FK,UK "unique with format"
        format varchar(10) UK "LARGE (500 KB at most) or SMALL (60 KB at most)"
        media_type varchar(30) "image/jpeg"
        content bytea "the resized image"
        created_at timestamp
        updated_at timestamp
    }

    product_translation {
        id varchar(50) PK
        product_id varchar(50) FK,UK "unique with language"
        language varchar(2) UK "en or fr: a product always has its row in en"
        name text "the name of the product in this language"
        description text
        created_at timestamp
        updated_at timestamp
    }

    stock {
        id varchar(50) PK
        product_id varchar(50) FK,UK "a product without row has none"
        quantity int "0 or more, orders awaiting payment reserved"
        created_at timestamp
        updated_at timestamp
    }

    cart_item {
        id varchar(50) PK
        cart_id varchar(50) FK,UK "unique with product_id"
        product_id varchar(50) FK,UK
        quantity int "greater than 0"
        created_at timestamp
        updated_at timestamp
    }

    product {
        id varchar(50) PK
        sku varchar(32) UK "Stock Keeping Unit: upper-case letters and digits, groups separated by hyphens"
        category varchar(30) "CLOTHING, ELECTRONICS or ACCESSORIES"
        price numeric
        currency varchar(3)
        vat_rate varchar(20) "STANDARD, INTERMEDIATE, REDUCED or SUPER_REDUCED: 20, 10, 5.5 or 2.1 %"
        created_at timestamp
        updated_at timestamp
    }

    shop_settings {
        id int PK "a single row (id = 1)"
        order_confirmation_email varchar(10) "ENABLED or DISABLED"
        payment_delay_minutes int "1 to 1440: time to pay an order"
        free_shipping_threshold numeric "standard shipping free from this price of the items (0 or more)"
        created_at timestamp
        updated_at timestamp
    }

    user ||--o{ email_verification : "user_id (cascade)"
    user ||--o| password_reset : "user_id (cascade)"
    user ||--o{ user_address : "user_id (cascade)"
    user_address }o--|| address : "address_id"
    user ||--o{ customer_order : "user_id (cascade)"
    customer_order }o--|| address : "delivery_address_id"
    customer_order }o--|| address : "billing_address_id"
    customer_order }o--o| payment : "payment_id"
    payment_refund |o--|| payment : "payment_id (cascade)"
    customer_order ||--|{ customer_order_line : "customer_order_id (cascade)"
    invoice |o--|| customer_order : "customer_order_id"
    invoice }o--|| seller : "seller_id"
    customer_order ||--|{ customer_order_status_change : "customer_order_id (cascade)"
    user |o--o| cart : "user_id (cascade)"
    cart ||--o{ cart_item : "cart_id (cascade)"
    cart_item }o--|| product : "product_id (cascade)"
    stock }o--|| product : "product_id (cascade)"
    product_image }o--|| product : "product_id (cascade)"
    product_translation }o--|| product : "product_id (cascade)"
```

## Notes

- Every table has a technical primary key `id` (generated by the application), and the creation and last update dates `created_at` / `updated_at` (set by Hibernate).
- An `address` belongs to no user: the users refer to their addresses through `user_address`, the orders through `delivery_address_id` and `billing_address_id`. Editing an address creates a new row, removing it from a user deletes only its `user_address` row: an order always keeps the address it was delivered and billed to.
- The `seller` written on the invoices is edited by the administrators. Each edit adds a version and never changes one: an `invoice` references the version current when it was issued, so that it keeps the seller it was issued with.
- `user` is quoted in SQL (`"user"`), a reserved word; the orders are stored in `customer_order` for the same reason.
- The name and the description of a `product` are in `product_translation`, one row per language: always one in the default language (`en`), which the application requires, and one per other language the product is translated into. A product without a translation in the language of the customer is shown in `en`.
