---
name: domain-driven-design
description: Domain-Driven Design and hexagonal architecture principles to follow when writing or reviewing back-end code - where business rules live, what entities, value objects and aggregates may reference, the role of use cases, domain services, repositories and adapters. Use before creating or changing an entity, aggregate, value object, use case, domain service, port (repository interface) or infrastructure adapter, and when reviewing domain code.
---

# Domain-Driven Design

Principles collected from the user's feedback. Apply them to any domain or infrastructure code and check them in reviews. When existing code breaks them, point it out instead of copying the flaw.

## 1. Adapters do not add business rules

An infrastructure adapter, and in particular the implementation of a domain repository, only translates and stores. It holds no business rule. Business rules are defined in the domain.

A repository adapter is limited to:
- converting between the domain model and the persistence model (JPA entity, document, SQL row);
- reading, inserting, updating and deleting what it is asked to;
- generating technical ids when needed.

Examples of business rules that do not belong in an adapter:
- "adding a product already in the cart changes the quantity of its line";
- "a quantity of zero removes the line";
- "the price of an item is the catalog price, not the one sent by the client";
- "an unknown product cannot be added".

Warning sign: an `if` on a business value (quantity, status, amount, existence of another object) in an adapter. Move the decision to the domain and give the port simple operations (`add`, `update`, `remove`, `findByX`, `load`, `save`).

A database constraint (foreign key, uniqueness, `CHECK`) may back a rule up to protect the data, but does not replace it: the rule is still written and tested in the domain.

Test these rules in the domain, without infrastructure (in-memory repositories or mocks); integration tests only check that the adapter stores and reads back correctly.

## 2. No repository in an entity, value object or aggregate

An entity, value object or aggregate of the domain does not reference a repository interface as a member (property, constructor parameter kept as a field). The entry point of a business action is a **use case**: it uses the repositories to load the domain objects, asks them to apply the rules, then saves the result.

Avoid:

```kotlin
class Cart(private val itemRepository: ItemRepository, private val productRepository: ProductRepository) {
    fun add(draftItem: DraftItem): CartItem { /* reads and writes through the repositories */ }
}
```

Prefer:

```kotlin
// Aggregate: state and rules, no dependency on infrastructure or ports
data class Cart(val items: List<Item>) {
    fun add(product: DefinedProduct, quantity: Quantity): Cart { /* pure rules */ }
}

// Use case: orchestrates the ports and delegates the rules to the aggregate
class AddItemToCartUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) : UseCase<AddItemToCartRequest> {
    // nominal step
    val product = productRepository.findById(request.productId) ?: throw ProductNotFoundException(...)
    cartRepository.save(cartRepository.load().add(product, request.quantity))
}
```

Why: the domain object stays testable without mocks, its rules can be read without following infrastructure calls, and each user journey has an explicit entry point (preconditions, nominal scenario, postconditions) that input adapters (REST controllers) call.

Consequences:
- a REST controller calls a use case (or a use case facade), never an entity built with repositories;
- rules that need external data (the catalog product) receive it as a parameter, loaded by the use case;
- a use case raises the business exceptions (`ProductNotFoundException`...) when loaded data is missing.

## 3. A use case holds no state between calls

A use case keeps nothing from one call to the next: no field set by a call, such as a `lateinit var createdProduct` read by the caller afterwards. A use case may be shared (a Spring singleton bean) and called by several requests at the same time: a result stored in a field can be overwritten by another request before the caller reads it.

Its only fields are its dependencies (repositories, ports), set once at construction. Everything about a call lives in the request object or in local variables.

To give a result back to the caller without storing it:
- let the caller choose the identity of what is created (`ProductId.nextId()`), pass it in the request, then read the created object with a read use case;
- or keep the call's data in local variables of the steps; when a postcondition needs data loaded during the scenario, check what can be read back from the repositories instead.

Avoid:

```kotlin
class CreateProductUseCase(private val productRepository: ProductRepository) : UseCase<Product> {
    lateinit var createdProduct: Product
    // nominal step: createdProduct = productRepository.save(product)
}
// caller: useCase.unfold(draft); return useCase.createdProduct
```

Prefer:

```kotlin
data class CreateProductRequest(val productId: ProductId, val draftProduct: DraftProduct)

class CreateProductUseCase(private val productRepository: ProductRepository) : UseCase<CreateProductRequest> {
    // nominal step: productRepository.save(request.definedProduct())
}
// caller: val productId = ProductId.nextId(); useCase.unfold(CreateProductRequest(productId, draft)); return readProductUseCase.getProduct(productId)
```

Creating a new use case instance for each call avoids the sharing, but the state stays a trap for the next caller who shares the instance: prefer stateless use cases anyway.

## 4. Package the domain by business function

The domain is split by business function, not by technical kind, so that the subdomains and bounded contexts emerge from the code: `org.flexstore.domain.order`, `org.flexstore.domain.cart`, `org.flexstore.domain.product`, `org.flexstore.domain.user`. A package holds everything of its function: entities, value objects, aggregates, use cases, repository ports, business exceptions.

When a functional package holds too many classes to read at a glance, split it inside with technical sub-packages:
- `model`: value objects, entities, aggregates (and their business exceptions);
- `usecase`: the use cases and their requests;
- `repository`: the repository ports.

A technical sub-package holds **at least 3 files**. Do not create a `model`, `usecase` or `repository` sub-package to move one or two files into it: keep them at the level of their functional package, next to the sub-packages that do exist. Create the sub-package once a third file of that kind arrives, and move the files back up if one of them goes away and fewer than 3 remain.

The elements common to several functions (shared value objects such as `Identity`, `Price`, `Quantity`, `Name`) go in a **shared kernel**: `org.flexstore.domain.sharedkernel`. Keep it small: an element used by a single function belongs to that function.

Dependencies between the technical sub-packages go one way:
- `model` depends on neither `usecase` nor `repository` (it may use the `model` of another function and the shared kernel);
- `repository` uses `model`;
- `usecase` uses `repository` and `model`.

So a model class never receives a use case request: when the model needs the data of a request, that data is a model value object, which the use case takes as its input (e.g. `Order.place(placement: OrderPlacement, cart)`, with `PlaceOrderUseCase : UseCase<OrderPlacement>`). The same direction applies inside a flat functional package, between the classes of each kind.

Avoid:

```
domain/entity/Order.kt
domain/repository/OrderRepository.kt
domain/usecase/order/PlaceOrderUseCase.kt
```

Prefer:

```
domain/order/Order.kt
domain/order/OrderRepository.kt
domain/order/PlaceOrderUseCase.kt
```

and, once the package grows:

```
domain/order/model/Order.kt, OrderLines.kt, OrderStatus.kt...
domain/order/usecase/PlaceOrderUseCase.kt, ReadOrdersUseCase.kt...
domain/order/OrderRepository.kt        (fewer than 3 ports: no repository/ sub-package)
```

Why: the package tree tells what the application does (orders, carts, catalog) before how it is built; the dependencies between functions become visible, which shows where the bounded contexts are.

## 5. Name things after the business

Methods, especially on repository ports, are named after the business question they answer, not after their technical implementation. Combine with the affirmative methods of the `fluent-code-reading-semantic` skill.

Avoid:

```kotlin
if (productRepository.findById(request.productId) == null) { ... }
```

Prefer:

```kotlin
if (productRepository.productIsMissing(request.productId)) { ... }
// and productRepository.productExists(productId) where the affirmative case is checked
```

Why: the condition reads as the business rule ("if the product is missing"), the null check and the lookup stay in the adapter.

The same goes for the classes of the domain themselves: their methods, especially factories, use the vocabulary of the domain, not technical verbs (`generate`, `create`, `build`, `process`, `handle`, `data`).

Avoid:

```kotlin
VisitorCartId.generate()
```

Prefer:

```kotlin
VisitorCartId.nextId()    // like Identity.nextId(), ProductId.nextId(), OrderId.nextId()
```

Use the same word for the same concept across the domain (`nextId()` for every new identity), so that a reader who knows one class knows the others.

## 6. Instantiating a domain class takes minimal work

Creating a domain object should not require the caller to know how its parts are built. When building a value takes more than passing data (generating an id, applying a default, combining values), give the class a factory method with a business name, and call it everywhere: the logic lives once, in the domain.

Avoid:

```kotlin
ProductId(Identity(java.util.UUID.randomUUID().toString()))
CartEntity(id = UUID.randomUUID().toString(), ...)          // the same logic repeated in an adapter
```

Prefer:

```kotlin
data class Identity(val value: String) {
    companion object {
        fun nextId() = Identity(UUID.randomUUID().toString())
    }
}

ProductId(Identity.nextId())
CartEntity(id = Identity.nextId().value, ...)
```

Why: the way ids (or any other derived value) are built is decided in one place; changing it (another id format, a sequence) changes one method; callers read what they get ("the next id"), not how it is made.

This applies to the adapters and the tests too: they reuse the domain factories (`Identity.nextId()`, `VisitorCartId.nextId()`) instead of repeating their logic.

## 7. A domain service, as a last resort

A business rule belongs first to an entity, an aggregate or a value object. When it cannot be placed in one of them, because it spans several objects, several use cases or several business functions (place the order, then confirm it by email; register a customer, then send them a verification link), a **domain service** can hold it and keep the domain model free of that logic.

A domain service is agnostic of the infrastructure: plain Kotlin, no framework annotation, no adapter, no HTTP or persistence type. Its dependencies are constructor parameters, wired by the infrastructure configuration.

Its name ends with **`Service`**, after the business journey it serves: `OrderCheckoutService`, `CustomerRegistrationService`, `ShopSettingsAdministrationService`. The suffix tells at once that the class is a domain service, not an entity or a use case; the rest of the name says which journey it serves. Avoid the vague suffixes `Manager`, `Helper` or `Processor`, and a name made of the suffix only (`OrderService`, which says nothing about what it does).

It plays one or both of these roles:
- **entry point of the domain, like a use case**: an input adapter (REST controller) calls it; it loads what it needs through the ports, asks the model to apply the rules and saves the result;
- **coordinator of use cases**: it calls one or several use cases, so their preconditions, nominal scenario and postconditions still apply, and passes the result of one to the next.

Rules:
- it acts on a repository **either** itself **or** through the use cases that use that repository, never both: when a use case already works with a repository, the service delegates to use cases for that repository (a read use case to read, the update use case to write) instead of also referencing the port;
- it holds no state between calls (section 3), and no rule that an entity, an aggregate or a value object could hold: it decides the order of the steps, the rules stay in the model and the use cases;
- it lives in the functional package of the journey it serves (section 4) and is tested in the domain, with in-memory repositories or mocks.

Before creating one, check the alternatives: a rule about one object goes into that object; a rule that needs external data receives that data as a parameter, loaded by the use case (section 2); a single business action is a single use case. A class that only forwards each call to one use case is a facade, not a domain service: it adds no logic of its own.

Avoid:

```kotlin
// The aggregate sends the email: the model depends on a port and on another journey
data class Order(...) {
    fun place(placement: OrderPlacement, cart: Cart, emailRepository: EmailRepository): Order { /* ... */ }
}

// Or the REST controller chains the use cases: the journey is decided in an adapter
placeOrderUseCase.unfold(placement)
sendOrderConfirmationEmailUseCase.unfold(readOrdersUseCase.getOrder(customer, orderId))

// Or the service references the repository next to the use case that already uses it
class ShopSettingsAdministrationService(
    private val shopSettingsRepository: ShopSettingsRepository,
    private val updateShopSettingsUseCase: UpdateShopSettingsUseCase,   // also uses ShopSettingsRepository
)
```

Prefer:

```kotlin
// Domain service: chains the use cases of the journey, the rules stay in the model and the use cases
class OrderCheckoutService(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val readOrdersUseCase: ReadOrdersUseCase,
    private val sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase,
) {
    fun placeOrder(customer: CartOwner.Customer): Order {
        val orderId = OrderId.nextId()
        placeOrderUseCase.unfold(OrderPlacement(orderId, customer, Instant.now()))
        val placedOrder = readOrdersUseCase.getOrder(customer.userId, orderId)
        sendOrderConfirmationEmailUseCase.unfold(placedOrder)
        return placedOrder
    }
}

// Delegation only: the settings are read and written through the use cases
class ShopSettingsAdministrationService(
    private val readShopSettingsUseCase: ReadShopSettingsUseCase,
    private val updateShopSettingsUseCase: UpdateShopSettingsUseCase,
)
```

Why: each use case stays a single, testable business action; the logic that combines them is written once, in the domain, instead of in a controller or inside an aggregate that would then depend on ports; acting on a repository from a single place keeps one path for each read and write.
