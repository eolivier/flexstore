---
name: fluent-code-reading-semantic
description: Principles that make code read like the business language - wrapping collections in their own class, naming boolean methods affirmatively, naming variables after their type, intermediate variables, strong types rather than nullable ones. Use before writing, changing or reviewing domain or application code (entities, value objects, aggregates, use cases), in any language.
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
- Add a qualifier when several variables have the same type, keeping the type in the name: `older` and `newer` orders, `deliveryAddress` and `billingAddress`, `updatedCart`, `storedOrder`.
- A value whose type is generic (`String`, `Boolean`, `Response`) is named after what it represents (principle 5).

## 8. Intermediate variables to read step by step

Do not nest calls inside the arguments of other calls. Give each intermediate value a variable named after what it is, then use the variables: the code reads as a sequence of sentences.

Avoid:

```kotlin
private fun placeOrder(orderId: String, owner: CartOwner.Customer, at: Instant) =
    orderRepository.save(Order.place(OrderPlacement(OrderId(orderId), owner, at, homeAddresses), Cart(owner).add(lamp, Quantity(1))))
```

Prefer:

```kotlin
private fun placeOrder(orderId: String, owner: CartOwner.Customer, at: Instant): Order {
    val orderPlacement = OrderPlacement(OrderId(orderId), owner, at, homeAddresses)
    val cart = Cart(owner).add(lamp, Quantity(1))
    val order = Order.place(orderPlacement, cart)
    return orderRepository.save(order)
}
```

Why: each line does one thing and names its outcome; the reader does not have to unfold the expression from the inside out to know what is built, in which order, and what is finally saved.

Details:
- A simple value object built inline stays inline (`Quantity(1)`, `OrderId(orderId)`): extract what is a step of the reasoning, not every constructor call.
- A chain that reads as a sentence (`cart.add(lamp, Quantity(2)).emptied()`) may stay on one line.

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
