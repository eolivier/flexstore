---
name: infrastructure-convention
description: Rules for the infrastructure layer of the back end (spring-infra) - JPA entities (naming, files, creation and update dates), persistence adapters and database migrations. Use before creating, renaming or changing a JPA entity, a Spring Data repository, a repository adapter or a Flyway migration, and when reviewing infrastructure code.
---

# Infrastructure convention

Rules collected from the user's feedback for the infrastructure layer (`backend/spring-infra`). Apply them to any new or changed infrastructure code and check them in reviews. When existing code breaks them, point it out (and fix it if you are already changing that code).

The business rules never live here: an adapter only translates and stores (see section 1 of the `domain-driven-design` skill).

## 1. A JPA entity is suffixed with `Entity`, and so is its file

The persistence model is kept apart from the domain model: a JPA entity is a class of the adapter (`org.flexstore.infra.spring.adapter.jpa`), never of the domain, and its name ends with **`Entity`**, after the concept it stores: `ProductEntity`, `CartItemEntity`, `OrderLineEntity`.

The `.kt` file has exactly the name of the class, suffix included, and holds one entity:

Avoid:

```
adapter/jpa/Product.kt     // holds ProductEntity
adapter/jpa/Order.kt       // holds OrderEntity and OrderLineEntity
```

Prefer:

```
adapter/jpa/ProductEntity.kt
adapter/jpa/OrderEntity.kt
adapter/jpa/OrderLineEntity.kt
```

Why: the file tree tells at once which files are persistence classes and which are adapters (`JpaProductRepository.kt`); a file named `Product.kt` reads as the domain product and hides a second class when it holds two.

## 2. Every JPA entity has a creation date and a last update date

Every JPA entity extends `TimestampedEntity`, the mapped superclass that holds `createdAt` and `updatedAt`:
- Hibernate sets them (`@CreationTimestamp`, `@UpdateTimestamp`): the adapters and the domain never set them;
- `created_at` is not updatable, so an entity rebuilt from the domain and saved again keeps its stored creation date.

A new entity therefore comes with a migration that creates its table with the two columns:

```sql
created_at TIMESTAMP NOT NULL DEFAULT now(),
updated_at TIMESTAMP NOT NULL DEFAULT now()
```

`EntityTimestampsIT` checks that every entity of the JPA metamodel has both dates: do not remove or bypass it.

Why: every stored row can be dated (when it was created, when it last changed) the same way, without code in each adapter.
