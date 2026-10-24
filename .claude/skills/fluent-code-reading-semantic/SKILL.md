---
name: fluent-code-reading-semantic
description: Principles that make code read like the business language - wrapping collections in their own class, naming boolean methods affirmatively instead of negating them. Use before writing, changing or reviewing domain or application code (entities, value objects, aggregates, use cases), in any language.
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
