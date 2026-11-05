---
name: domain-driven-design
description: Domain-Driven Design and hexagonal architecture principles to follow when writing or reviewing back-end code - where business rules live, what entities, value objects and aggregates may reference (value objects rather than primitive types), the role of use cases, domain services, repositories and adapters, groups of use cases named after their sub-domain (OrderUseCases, not a facade), every use case implementing the UseCase interface of ucop (or QueryUseCase when it answers), methods of the domain model that expose neither a primitive type nor a raw collection, the current moment read from InstantProvider (never a clock member), every administration use case checking in its own precondition that its actor is an administrator, one use case (not two) for a journey that the customer concerned or an administrator may run, a precondition or postcondition taken out of its use case staying in the usecase package (in a sub-package from more than 3). Use before creating or changing an entity, aggregate, value object, use case, domain service, port (repository interface) or infrastructure adapter, and when reviewing domain code.
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
- `usecase`: the use cases and their requests (never the domain services);
- `service`: the domain services (section 7);
- `repository`: the repository ports.

A domain service is not a use case: it never goes in `usecase`. It stays at the root of its functional package, next to the sub-packages that exist, until the package holds **at least 3 services**; from the third one, they go in a `service` sub-package.

The same goes for the use cases: as soon as a functional package contains **at least 3 use cases**, create its `usecase` sub-package and move them there (with their requests and the groups of use cases such as `OrderUseCases`), whatever the size of the package. With fewer than 3 use cases, they stay at the root of the package.

A technical sub-package holds **at least 3 files**. Do not create a `model`, `usecase` or `repository` sub-package to move one or two files into it: keep them at the level of their functional package, next to the sub-packages that do exist. Create the sub-package once a third file of that kind arrives, and move the files back up if one of them goes away and fewer than 3 remain.

The elements common to several functions (shared value objects such as `Identity`, `Price`, `Quantity`, `Name`) go in a **shared kernel**: `org.flexstore.domain.sharedkernel`. Keep it small: an element used by a single function belongs to that function.

Dependencies between the technical sub-packages go one way:
- `model` depends on neither `usecase` nor `repository` (it may use the `model` of another function and the shared kernel);
- `repository` uses `model`;
- `usecase` uses `repository` and `model`;
- `service` uses `usecase`, `repository` and `model`; no use case uses a service.

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
domain/order/service/OrderCheckoutService.kt, OrderAdministrationService.kt, UnpaidOrderService.kt
                                       (from 3 services; with fewer, they stay at the root: domain/order/OrderCheckoutService.kt)
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
- it lives in the functional package of the journey it serves, at its root or in its `service` sub-package from 3 services, never in `usecase` (section 4), and is tested in the domain, with in-memory repositories or mocks.

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

## 8. No primitive type in the domain model

An entity, an aggregate or a value object does not expose a primitive type (`String`, `Int`, `Long`, `BigDecimal`, `Instant`...) as a property, a constructor parameter or a return value: each business concept has its own value object, named after it.

The primitive only appears at the bottom, inside the value object that represents one single concept, as its `value`: `Street(val value: String)`, `Quantity(val value: Int)`. A composite value object, an entity or an aggregate is made of these value objects, never of the primitives themselves.

Avoid:

```kotlin
data class PostalAddress(
    val street: String,
    val postalCode: String,
    val city: String,
    val country: String
)

data class OrderLine(val productName: String, val quantity: Int)
```

Prefer:

```kotlin
data class Street(val value: String)
data class PostalCode(val value: String)
data class City(val value: String)
data class Country(val value: String)

data class PostalAddress(
    val street: Street,
    val postalCode: PostalCode,
    val city: City,
    val country: Country
)

data class OrderLine(val productName: Name, val quantity: Quantity)
```

Why: the type tells what a value is, so a postal code cannot be passed where a city is expected; the rules about one concept (a blank street, a valid postal code) live in its value object, once; and the signatures read in the language of the business.

Consequences:
- the adapters convert between the primitives of their technology (columns, JSON fields) and the value objects, at the edge of the domain;
- a method that answers a business question may return a `Boolean` (`isComplete()`, `isExpiredAt(instant)`): the question is in its name;
- a value object shared by several functions goes into the shared kernel (section 4).

## 9. A group of use cases is named after its sub-domain, in the plural: `OrderUseCases`

A class that holds references to several use cases of a sub-domain is named after that sub-domain followed by `UseCases`, in the plural: the plural says that it gathers several use cases (see principle 12 of the `fluent-code-reading-semantic` skill). Do not suffix it with `Facade`.

Avoid:

```kotlin
class OrderUseCaseFacade(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val readOrdersUseCase: ReadOrdersUseCase
)
```

Prefer:

```kotlin
class OrderUseCases(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val readOrdersUseCase: ReadOrdersUseCase
)
```

Why: the name says what the class is, the use cases of the orders, in the language of the domain; `Facade` names a technical pattern, says nothing of what is behind it, and reads as one object where there are several use cases.

Details:
- The name is the one of the sub-domain (the package of principle 4): `OrderUseCases`, `CartUseCases`, `ProductUseCases`, `PaymentUseCases`, `AddressUseCases`.
- A domain service that receives such a group keeps the `Service` suffix (principle 7): `PaymentService(paymentUseCases, ...)`.
- A group used only to pass use cases together to a service (a holder without behaviour) follows the same rule.

## 10. A use case implements the `UseCase` interface, or `QueryUseCase` when it answers

Every use case implements an interface of ucop (`org.ucop.domain.entity`), which gives it the same shape: preconditions, then a nominal scenario, then postconditions, run by `unfold`.
- **`UseCase<T>`** for a use case that changes the state (place an order, refund it, update a product): `unfold(t)` returns nothing; the caller reads the result back with a query (see section 3).
- **`QueryUseCase<T, R>`** for a use case that answers without changing anything (read, search, propose): its preconditions are checked on the request, its `NominalQuery` gives the answer, its postconditions are checked on the answer, and `unfold(t)` returns it.

The only exception is a class that aggregates several use cases: a group of use cases (`OrderUseCases`, section 9) or a domain service (section 7). It calls the `unfold` of the use cases it holds, and implements neither interface.

Avoid:

```kotlin
// A plain class: no preconditions, no postconditions, its rule is hidden in a method
class ProposeSkuUseCase(private val productRepository: ProductRepository) {
    fun proposeFor(skuBasis: SkuBasis): Sku =
        Sku.proposalsFor(skuBasis).first { sku -> productRepository.skuIsFree(sku) }
}
```

Prefer:

```kotlin
class ProposeSkuUseCase(private val productRepository: ProductRepository) : QueryUseCase<SkuBasis, Sku> {

    override fun getPreConditions(): PreConditions<SkuBasis> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<SkuBasis, Sku> { skuBasis ->
        Sku.proposalsFor(skuBasis).first { sku -> productRepository.skuIsFree(sku) }
    }

    // The answer is checked after: a SKU free in the catalog
    override fun getPostConditions(): PostConditions<Sku> = PostConditions(proposalIsFreeCondition())
}

// caller (the group of use cases): proposeSkuUseCase.unfold(skuBasis)
```

Why: every business action, read or write, reads the same way: what must hold before, what it does, what must hold after. The checks are written and logged once by ucop rather than left to each class, and a reader finds the rules of a use case in the same place every time.

Details:
- The name still ends with `UseCase` and says the action (`ReadOrdersUseCase`, `ProposeSkuUseCase`); a use case with several questions (`getOrders`, `getOrder`) is split into one query use case per question, or given a request type that carries the question.
- Existing use cases that are plain classes are pointed out in reviews and moved to `QueryUseCase` in a refactoring pull request of their own (see the `prompt-convention` skill).

## 11. The methods of the domain model expose neither a primitive type nor a raw collection

Every method of an entity, a value object or an aggregate, public or internal to the domain, takes and returns value objects and business collections: no primitive type (`Int`, `String`, `Long`, `BigDecimal`...) in its parameters nor in its return type, and no raw collection (`List`, `Set`, `Map`, `Sequence`).
- A number, a count, a text that the method needs or gives is a value object named after what it is: `LetterCount`, `WordCount`, `SkuNumber`, `SkuPrefix`.
- Several things of a kind are a business collection that wraps them (principle 1 of the `fluent-code-reading-semantic` skill): `Words` rather than `List<Word>`, `SkuProposals` rather than `Sequence<Sku>`. The wrapper keeps its collection private and offers the operations the domain needs (`firstOnes`, `shortenedTo`, `firstAccepted`).

This extends section 8, which already covers the properties and the constructors, to every method.

Avoid:

```kotlin
data class Words(private val words: List<String>) {
    fun firstOnes(count: Int): List<String> = words.take(count)
    fun asList(): List<String> = words
}

data class SkuPrefix(val value: String) {
    fun numbered(number: Int) = Sku("$value-${number.toString().padStart(4, '0')}")
}

fun proposalsFor(skuBasis: SkuBasis): Sequence<Sku> = ...
```

Prefer:

```kotlin
data class Word(val value: String) {
    fun shortenedTo(letterCount: LetterCount) = Word(value.take(letterCount.value))
}

data class Words(private val words: List<Word>) {
    fun firstOnes(wordCount: WordCount) = Words(words.take(wordCount.value))
    fun hyphenated() = SkuPrefix(words.joinToString("-") { it.value })
}

data class SkuPrefix(val value: String) {
    fun numbered(skuNumber: SkuNumber) = Sku("$value-${skuNumber.value.toString().padStart(4, '0')}")
}

data class SkuProposals(private val prefix: SkuPrefix) {
    fun firstAccepted(accepts: (Sku) -> Boolean): Sku = ...
}
```

Why: the signature tells what each value is (a count of letters, the number of a SKU), so values of different meanings cannot be swapped; the rules on them have one home; and a business collection names what its elements form and offers only the operations that make sense, instead of every operation of a `List`.

Details:
- The primitive stays inside the value object of a single concept, as its `value` (section 8), and inside the body of the methods.
- A method that answers a business question may return a `Boolean` (`isWellFormed()`, `skuIsFree(sku)`): the question is in its name.
- A factory that converts what an adapter receives may take a primitive (`Sku.from(typedSku: String)`, `Words.of(text: String)`): it is the edge of the domain.
- A read-only view of a collection (`asList()`) remains only for the adapters that must iterate to map to JSON or to the persistence model; the domain itself never calls it.
- Existing methods that break this rule are pointed out in reviews and changed in a refactoring pull request of their own (see the `prompt-convention` skill).

## 12. The current moment comes from `InstantProvider`, never from a clock member

The domain reads the current moment from **`InstantProvider.now()`**, the object of the shared kernel (`org.flexstore.domain.sharedkernel`). A use case, a domain service or a factory never holds a `Clock` as a member or a constructor parameter, and never calls `Instant.now()` or `Clock.systemUTC()` itself.

Avoid:

```kotlin
class PasswordResetService(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val clock: Clock = Clock.systemUTC()
) {
    fun requestReset(email: Email) =
        requestPasswordResetUseCase.unfold(PasswordResetRequest(email, clock.instant()))
}

// test
val service = PasswordResetService(requestPasswordResetUseCase, Clock.fixed(now, ZoneOffset.UTC))
```

Prefer:

```kotlin
class PasswordResetService(private val requestPasswordResetUseCase: RequestPasswordResetUseCase) {
    fun requestReset(email: Email) =
        requestPasswordResetUseCase.unfold(PasswordResetRequest(email, InstantProvider.now()))
}

// A moment of the business gets its factory, read from InstantProvider
data class StatusChangedAt(val value: Instant) {
    companion object {
        fun now() = StatusChangedAt(InstantProvider.now())
    }
}

// test: the moment is fixed for the test, then reset
@BeforeEach
fun fixTheMoment() {
    InstantProvider.fixAt(now)
}

@AfterEach
fun resetTheMoment() {
    InstantProvider.reset()
}
```

Why: the current moment is a concern of the whole domain, not a dependency of each class. A clock member adds a technical parameter to every constructor that needs the time, and a default value (`Clock.systemUTC()`) that the Spring configuration and the tests must remember to override. With a single provider, the time is read the same way everywhere, and a test fixes it in one place.

Details:
- The moment that enters a use case is read once, by the use case or the domain service that starts the journey, and passed in its request (`PasswordResetRequest(email, InstantProvider.now())`), so that every rule of the call uses the same moment.
- A test that depends on the moment fixes it with `InstantProvider.fixAt(...)` and resets it in an `@AfterEach`, so that the next tests read the time of the system again.
- The infrastructure is not concerned: an adapter that needs the time for a technical reason (the expiry of a session token) may read it itself.

## 13. An administration use case checks in its precondition that its actor is an administrator

Every use case that only an administrator may run (changing the catalog, the stocks, the seller, the legal pages, the discount codes, the settings, the users, administering the orders, reading what only the administration reads) receives **the user who runs it** in its request, and checks in **a private precondition of its own** that this user is an existing user with the ADMIN role, read from the users (`userRepository.isNotAdministrator(...)`), not from the request. The check is not left to the security configuration of the adapter alone (`/api/admin/**`), and it is not shared in a generic precondition class: each use case throws its own exception, with a message that names what was refused.

Avoid:

```kotlin
// Only the URL protects the rule: another adapter (a batch, a message listener) could run it for anyone
class ChangeStockAlertUseCase(private val stockRepository: StockRepository) : UseCase<StockAlert> {
    override fun getPreConditions(): PreConditions<StockAlert> = PreConditions(thresholdIsNotNegativeCondition())
    ...
}

// A generic precondition, shared by every use case: a generic message, and the rule hidden in another class
class IsAdministratorPreCondition<T>(private val actorOf: (T) -> UserId) : PreCondition<T> { ... }
```

Prefer (as `PublishLegalPageUseCase`):

```kotlin
// The request names its actor after its role in the journey: editor, reader, publisher, creator...
data class StockAlertChange(val editor: UserId, val stockAlert: StockAlert)

class ChangeStockAlertUseCase(
    private val stockRepository: StockRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : UseCase<StockAlertChange> {

    override fun getPreConditions(): PreConditions<StockAlertChange> = PreConditions(
        editorIsAdministratorCondition(), productExistsCondition(), thresholdIsNotNegativeCondition()
    )
    ...

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<StockAlertChange> { change ->
        if (userRepository.isNotAdministrator(change.editor)) {
            throw StockAlertForbiddenException(
                ErrorMessage("Only an administrator can change the alert threshold of a product.")
            )
        }
    }
}

// The controller passes the signed-in user, read from its token
@PutMapping("/api/admin/stocks/{productId}/alert")
fun changeStockAlert(@AuthenticationPrincipal editor: Jwt, ...): JsonStockAlert =
    JsonStockAlert.of(stockAlertUseCases.change(UserId.of(editor.subject), stockAlert))
```

Why: who may run a use case is a rule of the business, so it belongs to the use case like its other rules; it then holds whatever the adapter that calls it, and it is tested with the use case. Reading the role from the users, not from the token, refuses a user whose role was taken back since the token was issued.

Details:
- **The first precondition**: the actor is checked before the other rules, so that a refused user learns nothing about the data (an unknown product, an invalid value).
- **Its own exception and message** per sub-domain or use case (`StockAlertForbiddenException`, "Only an administrator can …"), mapped to 403 by the `RestExceptionHandler`.
- **Query use cases too**: a `QueryUseCase` that only the administration reads takes a request with its reader (`LowStocksReading(reader)`), even when it needs nothing else.
- **The group of use cases** takes the actor as the first argument of its methods (`stockAlertUseCases.change(editor, stockAlert)`), and passes it to each use case it unfolds.
- **The tests** of the use case check that an administrator runs it, and that a customer and an unknown user are refused.

## 14. One use case for a journey that the customer concerned or an administrator may run

When the same journey may be run by the customer it concerns or by an administrator (reading an order, downloading its invoice or its credit note), it is **one use case**, not a customer version and an administration version. Its request names the user who runs it, and its precondition accepts **either an administrator or the customer the data belongs to**.

Avoid:

```kotlin
// Two use cases that read the same order: the rules are written twice and drift apart
class ReadAdministeredOrderUseCase(...) : QueryUseCase<AdministeredOrderReading, AdministeredOrder>  // admin only
class ReadCustomerOrderWithCustomerUseCase(...) : QueryUseCase<CustomerOrderLookup, AdministeredOrder>  // owner only
```

Prefer:

```kotlin
// The reader is an administrator, or the customer of the order
data class AdministeredOrderReading(val reader: UserId, val orderId: OrderId)

class ReadAdministeredOrderUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<AdministeredOrderReading, AdministeredOrder> {

    override fun getPreConditions(): PreConditions<AdministeredOrderReading> =
        PreConditions(orderIsReadableByTheReaderCondition())
    ...

    // An administrator reads any order, a customer their own ones; the order of another customer is reported as
    // not found, so that its existence is not revealed
    private fun orderIsReadableByTheReaderCondition() = PreCondition<AdministeredOrderReading> { reading ->
        if (orderIsMissingFor(reading)) {
            throw OrderNotFoundException(ErrorMessage("Order ${reading.orderId.value} not found."))
        }
    }
}
```

Why: a journey is one set of business rules (preconditions, scenario, postconditions); two use cases for two kinds of actor duplicate them and let them drift apart. Who may run it is one of these rules, written once, in one precondition.

Details:
- **The domain service or the group of use cases** offers one method for both (`invoiceService.invoice(reader, orderId)`), which the customer's and the administration's endpoints both call.
- **A journey reserved to the administrators** (section 13) stays checked by its own administrator precondition; this section is for the journeys open to the customer concerned as well.
- **The tests** check that an administrator and the customer concerned run it, and that another customer and an unknown user are refused.

## 15. A precondition or postcondition taken out of its use case stays in the `usecase` package

A precondition or a postcondition is first written as a private method of its use case (sections 10 and 13). When it is taken out into a class or a file of its own (because several use cases share it, or because it is too long), it stays in the `usecase` package of its functional package: it is part of the use case's scenario, so it never goes to `model`, `service`, `repository` nor a shared package outside `usecase`.

From **more than 3** externalized conditions in the functional package, group them in a sub-package of `usecase` (`usecase/precondition` for the preconditions, `usecase/postcondition` for the postconditions) instead of leaving them next to the use cases. With 3 or fewer, they stay at the root of `usecase`; move them back up if the count falls to 3.

Avoid:

```
domain/order/model/OrderIsPlaceableCondition.kt        (a condition in the model)
domain/order/usecase/ (use cases + 5 externalized conditions mixed together)
```

Prefer:

```
domain/order/usecase/PlaceOrderUseCase.kt, ReadOrdersUseCase.kt...
domain/order/usecase/precondition/CartIsNotEmptyPreCondition.kt, ...    (more than 3)
domain/order/usecase/postcondition/OrderIsSavedPostCondition.kt, ...
```

Why: the conditions are read with the use cases that run them, and the dependency direction of section 4 is kept (`model` knows nothing of the use cases); a sub-package keeps the folder of the use cases readable once the conditions outnumber them.
