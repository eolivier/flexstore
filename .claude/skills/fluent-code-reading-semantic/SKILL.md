---
name: fluent-code-reading-semantic
description: Principles that make code read like the business language - wrapping collections in their own class, naming boolean methods affirmatively, naming variables after their type, intermediate variables, strong types rather than nullable ones, one or two arguments per method, lines of 113 characters at most, singular or plural names, no repeated names, no null arguments, beans declared step by step (repository, use case, service). Use before writing, changing or reviewing domain or application code (entities, value objects, aggregates, use cases), in any language.
---

# Fluent code reading semantic

Principles collected from the user's feedback, so that code reads as a sentence of the business language. Apply them to the code you write or change, and check them in reviews. When existing code breaks them, point it out (and fix it if you are already changing that code).

## 1. Wrap a collection in its own class

Do not pass or expose a raw `List` (or `Set`, `Map`) of domain objects. Wrap it in a class named after the business concept, which holds the collection and the behaviour on it.

Example: `Items` wraps a list of `Item` and knows how to compute its total price.

Avoid:

```kotlin
data class Cart(val owner: CartOwner, val items: List<Item>) {
    fun totalPrice() = items.fold(BigDecimal.ZERO) { total, item -> total + item.price() }
    fun findBy(productId: ProductId) = items.firstOrNull { it.productId == productId }
}
```

Prefer:

```kotlin
data class Items(private val items: List<Item>) {
    fun totalPrice(): Price = ...
    fun findBy(productId: ProductId): Item? = ...
    fun add(item: Item): Items = Items(items + item)
    fun isEmpty() = items.isEmpty()
}

data class Cart(val owner: CartOwner, val items: Items) {
    fun totalPrice() = items.totalPrice()
}
```

Why: the operations on the collection (searching, totals, adding, removing, invariants such as "a product at most once") get a name and a single home instead of being repeated as `filter` / `fold` / `firstOrNull` wherever the list is used; the owner of the collection reads in business terms.

Details:
- The wrapper is immutable: its operations return a new instance.
- Keep the raw collection private; expose business operations, and a read-only view only where an adapter needs to iterate (mapping to JSON or to the persistence model).
- Name the wrapper after the business concept (`Items`, `Products`, `OrderLines`), not after its type (`ItemList`).

## 2. Prefer affirmative methods

Name boolean methods so that callers state what they check, without negating another method. Add the affirmative method to the class when it is missing.

Avoid:

```kotlin
if (!quantity.isPositive()) { ... }
```

Prefer:

```kotlin
if (quantity.isZeroOrNegative()) { ... }
```

Why: a condition reads as the rule it expresses ("if the quantity is zero or negative"); a `!` is easy to miss when reading and doubles the effort to understand the condition.

Details:
- Each predicate and its opposite may both exist (`isPositive()` / `isZeroOrNegative()`, `exists()` / `notExists()`), so that every caller uses the affirmative form of what it means.
- The same applies to local booleans: `val unchanged = ...` checked as `if (unchanged)` rather than `val changed = ...` checked as `if (!changed)`.

## 4. Simple constructors: the class does the recurring work

Constructors and factory methods take what the caller naturally has, and the class does the recurring work of building its state. A caller should not wrap, convert or assemble values that the class could handle itself.

Avoid:

```kotlin
Items(listOf(item1))
Cart(customer, Items(listOf(item1, item2)))
Orders(listOf(older, newer))
```

Prefer:

```kotlin
data class Items(private val items: List<Item>) {
    constructor(vararg items: Item) : this(items.toList())
}
data class Cart(val owner: CartOwner, val items: Items) {
    constructor(owner: CartOwner, vararg items: Item) : this(owner, Items(*items))
}

Items(item1)
Cart(customer, item1, item2)
Orders(older, newer)
```

Why: the collection, the conversion or the default is an internal choice of the class; when it lives in the class, callers read the business values only and the choice can change in one place.

Details:
- Keep the constructor taking the internal form (a `List`) for the adapters that already have a list; add the convenient one (`vararg`, single value) next to it.
- With a `vararg` constructor, drop the default value of the primary one (`= emptyList()`): `Items()` then has a single meaning.
- The same goes for factory methods: `Identity.nextId()` rather than `Identity(UUID.randomUUID().toString())` (see the `domain-driven-design` skill, principle 6).

## 5. Names that say what the thing is

Name variables, parameters, types and classes after what they represent in the business, never with generic words that would fit anything: `result`, `data`, `value` (outside a value object's own field), `info`, `item` (when it is not an item of the domain), `obj`, `tmp`, `manager`, `processor`, `handler`, `helper`, `util`, `process`, `handle`, `do`.

Avoid:

```kotlin
val result = bcryptVerifier.verify(password, hash)
```
```ts
const result = await ordersStore.placeOrder();
const data: LoginResponse = await response.json();
type FetchOrderResult = ...
```

Prefer:

```kotlin
val verification = bcryptVerifier.verify(password, hash)
```
```ts
const placement = await ordersStore.placeOrder();
const session: LoginResponse = await response.json();
type OrderLookup = ...
```

Why: a generic name forces the reader to go back to where the value comes from to know what it holds; a business name carries that meaning wherever the value is used.

Details:
- In a test, name the outcome after what it is: `val response = mockMvc.perform(...)`, `val updatedCart = cart.add(...)`, `val thrown = catchThrowable { ... }`.
- Names imposed by a framework or a pattern keep their name (`EnvironmentPostProcessor`, `@ExceptionHandler`, the `handle` of a chain of responsibility).

## 6. No boolean argument: one method per intent

A constructor or a function does not take a boolean argument. A call such as `ShopSettings(true)` or `showFeedback(false, text)` does not say what `true` or `false` means; the implementation exposes instead one method per need, whose name says it.

Avoid:

```kotlin
data class ShopSettings(val orderConfirmationEmailEnabled: Boolean)
facade.updateSettings(ShopSettings(orderConfirmationEmailEnabled = false))
```
```ts
showFeedback(false, t('profile.passwordError'));
```

Prefer:

```kotlin
enum class Activation { ENABLED, DISABLED }

data class ShopSettings(val orderConfirmationEmail: Activation) {
    fun sendsOrderConfirmationEmail() = orderConfirmationEmail == Activation.ENABLED
    fun withOrderConfirmationEmailDisabled() = copy(orderConfirmationEmail = Activation.DISABLED)
    companion object { fun byDefault() = ShopSettings(Activation.ENABLED) }
}

facade.disableOrderConfirmationEmail()
```
```ts
showError(t('profile.passwordError'));
```

Why: each call reads as the intent ("disable the order confirmation email"), and a new case gets a new name instead of a new combination of flags.

Details:
- A state with two values that must be stored is a small enum named after the business (`Activation`), not a `Boolean`; the same holds for the API (`/order-confirmation-email/enable` and `/disable`, or `"ENABLED"`) and the database column.
- Returning a boolean is fine (`sendsOrderConfirmationEmail()`, `orderExists(id)`); it is the boolean argument that hides the intent.
- Arguments imposed by a framework keep their type (the `modelValue` of a Vue `v-model`).

## 7. Name a variable after its type

When a variable holds a domain object, name it after its type, in lower camel case: an `OrderPlacement` is `orderPlacement`, a `Cart` is `cart`, an `OrderAddresses` is `orderAddresses`. Do not use the name of its role in the code (`request`, `command`, `input`, `param`, `payload`).

Avoid:

```kotlin
private val request = OrderPlacement(OrderId("order-1"), customer, placedAt, homeAddresses)
val placeOrderStep = Step<OrderPlacement> { request -> ... }
```

Prefer:

```kotlin
private val orderPlacement = OrderPlacement(OrderId("order-1"), customer, placedAt, homeAddresses)
val placeOrderStep = Step<OrderPlacement> { orderPlacement -> ... }
```

Why: the reader knows at once what the variable holds and which operations it offers, wherever it is used, without going back to its declaration.

Details:
- The same goes for parameters, which take the domain type rather than a primitive: `orderId: OrderId`, not `orderId: String` converted inside the function (see principle 8 of the `domain-driven-design` skill).
- Name a test fixture after what it is in the business, never with a shortened or vague name: `deskLamp`, `homeAddress`, `customer`, not `lamp`, `addr`, `owner`.
- Add a qualifier when several variables have the same type, keeping the type in the name: `older` and `newer` orders, `deliveryAddress` and `billingAddress`, `updatedCart`, `storedOrder`.
- A value whose type is generic (`String`, `Boolean`, `Response`) is named after what it represents (principle 5).

## 8. Intermediate variables to read step by step

Do not nest calls inside the arguments of other calls. Give each intermediate value a variable named after what it is, then use the variables: the code reads as a sequence of sentences.

Avoid:

```kotlin
private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))

private fun placeOrder(orderId: OrderId, customer: CartOwner.Customer, placedAt: Instant) =
    orderRepository.save(Order.place(OrderPlacement(orderId, customer, placedAt, homeAddresses), Cart(customer).add(deskLamp, Quantity(1))))
```

Prefer:

```kotlin
private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))

private fun placeOrder(orderId: OrderId, customer: CartOwner.Customer, placedAt: Instant): Order {
    val orderPlacement = OrderPlacement(orderId, customer, placedAt, homeAddresses)
    val cart = Cart(customer).add(deskLamp, Quantity(1))
    val order = Order.place(orderPlacement, cart)
    return orderRepository.save(order)
}
```

Why: each line does one thing and names its outcome; the reader does not have to unfold the expression from the inside out to know what is built, in which order, and what is finally saved.

Details:
- A simple value object built inline stays inline (`Quantity(1)`, `Name("Desk lamp")`): extract what is a step of the reasoning, not every constructor call.
- A chain that reads as a sentence (`cart.add(deskLamp, Quantity(2)).emptied()`) may stay on one line.

## 9. Strong types rather than nullable types

Avoid nullable types (`Address?`, `String?`) as much as possible. When the business says a value is always there, its type says it too: a non-null property, filled when the object is created. When a value may really be missing, give the absence a name with a type instead of `null`: a sealed class, a dedicated value, or a collection that may be empty.

Avoid:

```kotlin
data class Order(..., val addresses: OrderAddresses? = null)

addresses = if (deliveryAddress != null && billingAddress != null) {
    OrderAddresses(deliveryAddress.toDomain(), billingAddress.toDomain())
} else null
```

Prefer:

```kotlin
// An order always has a delivery address and a billing address
data class Order(..., val addresses: OrderAddresses)

addresses = OrderAddresses(deliveryAddress.toDomain(), billingAddress.toDomain())
```

Why: a nullable type spreads `?.`, `?:` and `if (x != null)` to every caller, and each of them must decide what the absence means; a strong type states the rule once, and the compiler checks it everywhere.

Details:
- Do not make a property nullable to support old data: migrate the data (see the `database-convention` skill) so that the type stays strong.
- A real absence is named: `PostalAddresses.none()`, `User.UndefinedUser`, rather than `null`.
- Nullable types remain where a framework imposes them (an optional JSON field, a nullable column of a JPA entity mapped from an optional value): convert them at the boundary, in the adapter.

## 10. One argument per method and constructor, two at most

A method or a constructor ideally takes a single argument, two at most. When it needs more values, group the ones that go together into an intermediate class named after the business concept, and pass that class.

Avoid:

```kotlin
val user = User(userId, firstName, lastName)
```

Prefer:

```kotlin
val fullName = FullName(firstName, lastName)
val user = User(userId, fullName)
```

Why: each argument is one concept that reads at once; the intermediate class names what the values form together (a full name, an address, a placement), holds their rules (`FullName.isComplete()`), and can be passed on without spreading its parts across the signatures.

Details:
- Look for values that always travel together (a first name and a last name, a street and a city, an amount and a currency): they are a value object (`FullName`, `PostalAddress`, `Price`).
- The intermediate class is built in its own variable before the call (principle 8), so that the call reads with its business arguments.
- A class may still have more properties than that, built from such intermediate classes; the rule bounds what each call has to pass.
- An existing signature with more arguments is pointed out in reviews, and changed when the code is changed anyway; a JPA entity or a JSON class mapping each column or field keeps one property per column.

## 11. A line of code is 113 characters at most

No line of code is longer than 113 characters, indentation included. A line that would be longer is split by introducing intermediate variables named after what they hold (principle 8), not by wrapping the arguments of a single expression over several lines.

Avoid:

```kotlin
private val order = Order.place(OrderPlacement(OrderStamp(OrderId("order-1"), placedAt), Checkout(owner, homeOrderAddresses)), Cart(owner).add(deskLamp, Quantity(2)))
```

Prefer:

```kotlin
private val orderStamp = OrderStamp(OrderId("order-1"), placedAt)
private val orderPlacement = OrderPlacement(orderStamp, Checkout(owner, homeOrderAddresses))
private val cart = Cart(owner).add(deskLamp, Quantity(2))
private val order = Order.place(orderPlacement, cart)
```

Why: a line that fits on the screen is read at once, without scrolling or following a wrapped expression; the intermediate variables that keep it short also name each step.

Details:
- The limit applies to every language of the project (Kotlin, TypeScript, Vue, SQL); a long string or a URL that cannot be split is the only exception.
- When the variables are not enough (a declaration with many parameters), put one parameter per line, as the existing data classes do.
- Comments follow the same limit: split them over several lines.

## 12. Singular or plural: one thing, or a collection of things

The name of a class is singular when an instance is one thing, plural only when it wraps a collection of those things (principle 1).

Avoid:

```kotlin
// One detail of an order (its placement and its lines), not several details
data class OrderDetails(val placement: OrderPlacement, val lines: OrderLines)
```

Prefer:

```kotlin
data class OrderDetail(val placement: OrderPlacement, val lines: OrderLines)

// A collection of order details: the plural is then appropriate
data class OrderDetails(private val orderDetails: List<OrderDetail>)
```

Why: the reader expects one value behind a singular name and a collection behind a plural one; a plural name on a single value suggests operations (iterate, count, search) that it does not offer.

Details:
- A plural class is the wrapper of the singular one: `Items` wraps `Item`, `OrderLines` wraps `OrderLine`, `PostalAddresses` wraps `PostalAddress`.
- Variables and properties follow the same rule: `orderDetail` for one, `orderDetails` for the collection.

## 13. Do not repeat a name: `location = location`

When the value passed has the same name as the parameter or the property it fills, do not write the name twice. Pass it positionally in Kotlin, or use the shorthand property in TypeScript; when a named argument is the only way (a `copy` of a data class), build the object with its constructor instead.

Avoid:

```kotlin
private fun addressAt(location: Location) = address.copy(location = location)
CustomerRegistration(userId = userId, fullName = fullName, email = email)
```
```ts
const postalAddress = { firstName: firstName, lastName: lastName };
```

Prefer:

```kotlin
private fun addressAt(location: Location) = PostalAddress(janeDoe, location)
CustomerRegistration(userId, fullName, email)
```
```ts
const postalAddress = { firstName, lastName };
```

Why: the repeated name adds nothing to read, and hides among the arguments the one that really says something (`city = City("")`).

Details:
- A named argument stays useful when the name says what the value means and the value does not (`quantity = 1`, `status = AccountStatus.ACTIVE`), or to skip default parameters.
- A mapping between layers with different names keeps the names (`street = location.street.value`).

## 14. No `null` argument: a constructor without it

A caller never passes `null` to say that an optional value is missing. The class declares a constructor (or a factory method) without that argument, which fills the absence itself.

Avoid:

```kotlin
private val officeLocation = Location(officeStreet, null, PostalCode("69001"), lyon, france)
```

Prefer:

```kotlin
data class Location(
    val street: Street,
    val complement: AddressComplement?,
    val postalCode: PostalCode,
    val city: City,
    val country: Country
) {
    // An address without complement (building, floor...)
    constructor(street: Street, postalCode: PostalCode, city: City, country: Country) :
        this(street, null, postalCode, city, country)
}

private val officeLocation = Location(officeStreet, PostalCode("69001"), lyon, france)
```

Why: a `null` among the arguments says nothing about what is missing, and the reader has to look up the signature to know which value it stands for; the constructor without the argument names the case, and keeps the absence inside the class (where principle 9 may later replace it with a named value).

Details:
- The same goes for any function: `send(email)` next to `send(email, attachment)`, rather than `send(email, null)`.
- An adapter that maps a value which may be missing (a nullable column, an optional JSON field) may still pass it through: `complement?.let { AddressComplement(it) }` is a value, not a literal `null`.

## 15. Beans declared step by step: repository, use case, service

In the Spring configuration, each layer is its own bean, which receives the beans of the layer below:
1. the **repository** (the adapter of a port) is a bean;
2. each **use case** is a bean that receives its repositories;
3. the **domain service** (or the facade) is a bean that receives its use cases.

A bean never builds the objects of another layer inline (`PaymentService(StartPaymentUseCase(orderRepository, ...), ...)`).

Avoid:

```kotlin
@Bean
open fun paymentService(paymentRepository: PaymentRepository, orderRepository: OrderRepository,
                        cartRepository: CartRepository): PaymentService {
    val paymentUseCases = PaymentUseCases(
        StartPaymentUseCase(orderRepository, paymentRepository),
        ConfirmPaymentUseCase(orderRepository, cartRepository),
        ReadPaymentUseCase(paymentRepository)
    )
    return PaymentService(paymentUseCases, ...)
}
```

Prefer:

```kotlin
@Bean
open fun startPaymentUseCase(orderRepository: OrderRepository, paymentRepository: PaymentRepository) =
    StartPaymentUseCase(orderRepository, paymentRepository)

@Bean
open fun confirmPaymentUseCase(orderRepository: OrderRepository, cartRepository: CartRepository) =
    ConfirmPaymentUseCase(orderRepository, cartRepository)

@Bean
open fun paymentService(paymentUseCases: PaymentUseCases, paidOrderUseCases: PaidOrderUseCases) =
    PaymentService(paymentUseCases, paidOrderUseCases)
```

Why: the configuration reads as the architecture (each layer depends only on the layer below), every use case exists once and can be shared, and a bean says in its signature what it needs.

Details:
- When the configuration class grows too big, split it by layer: `RepositoryConfig` (the repositories that are not already components), `UseCaseConfig` (the use cases), `ServiceConfig` (the domain services and facades).
- A group of use cases passed together (`PaymentUseCases`) is a bean too, built from the use case beans.
