---
name: semantic-test-principles
description: Principles for the structure and wording of automated tests (unit, integration, front-end). Use before writing, changing or reviewing any test, in any language or framework (JUnit, Kotest, Vitest...).
---

# Semantic test principles

Principles collected from the user's feedback. Apply them to every test you write or change, and check them in reviews. When an existing test breaks them, point it out (and fix it if you are already changing that test).

## 1. Arrange, Act, Assert

Every test is structured in three sections, marked with the comments `// Arrange`, `// Act` and `// Assert`, in that order:
- **Arrange**: build the context (objects, repositories, mocks, initial state, request);
- **Act**: perform the single action under test, usually one call;
- **Assert**: check the outcome.

```kotlin
@Test
fun `adding a product already in the cart should change the quantity of its item`() {
    // Arrange
    val item = CartItem(ItemId(Identity("item-1")), product, Quantity(1))
    val cart = Cart(listOf(item))
    // Act
    val updatedCart = cart.add(product, Quantity(3))
    // Assert
    assertThat(updatedCart.items).containsExactly(item.copy(quantity = Quantity(3)))
}
```

```ts
it('should report a wrong current password', async () => {
  // Arrange
  const store = await signIn();
  mockFetch.mockResolvedValueOnce({ ok: false, status: 400 });
  // Act
  const result = await store.changePassword('wrong', 'new-password');
  // Assert
  expect(result).toBe('wrongPassword');
});
```

Details:
- Use exactly these words, not `given / when / then` or other variants.
- No blank line between the sections, nor inside them: the three comments already space out the test for reading.
- Keep each section once and in order; do not interleave several Act and Assert blocks. A test that needs two actions to check two behaviours is two tests.
- When the action is expected to fail, capture the failure in Act and check it in Assert, so the sections stay separate:

  ```kotlin
  // Act
  val thrown = catchThrowable { cart.add(product, Quantity(0)) }
  // Assert
  assertThat(thrown).isInstanceOf(InvalidQuantityException::class.java)
  ```

- Setup shared by all the tests of a class (fields, `@BeforeEach`, `beforeEach`) is part of Arrange; a test with nothing more to arrange may omit its `// Arrange` section, but keeps `// Act` and `// Assert`.

## 2. Parameterized tests for boundary cases

Prefer a parameterized test to cover the boundary cases of a rule (zero, negative, limit, just above the limit, empty, missing...), instead of copying the same test for each value or packing several values into one long test. The test stays one Arrange / Act / Assert, run once per case.

Avoid:

```kotlin
@Test
fun `an item whose quantity becomes zero should be removed`() { ... changeQuantity(item1.itemId, Quantity(0)) ... }

@Test
fun `an item whose quantity becomes negative should be removed`() { ... changeQuantity(item1.itemId, Quantity(-1)) ... }
```

Prefer:

```kotlin
@ParameterizedTest(name = "a quantity of {0} removes the item")
@ValueSource(ints = [0, -1, -10])
fun `an item whose quantity becomes zero or negative should be removed`(quantity: Int) {
    // Arrange
    val cart = Cart(customer, listOf(item1))
    // Act
    val updatedCart = cart.changeQuantity(item1.itemId, Quantity(quantity))
    // Assert
    assertThat(updatedCart.items).isEmpty()
}
```

```ts
it.each([0, -1, -10])('a quantity of %i removes the item', async (quantity) => {
  // Arrange
  ...
  // Act
  ...
  // Assert
  ...
});
```

The parameterized test must stay readable:
- Give each case a name that reads as the rule (`name = "a quantity of {0} removes the item"`, `'a quantity of %i...'`), so that a failure says which case broke.
- Keep few parameters, ideally one or two: the input and, when it varies, the expected outcome (`@CsvSource("0, true", "1, false")`). More columns mean the cases are different behaviours: write separate tests.
- One behaviour per parameterized test: cases with different expected outcomes in kind (removed vs rejected) are separate tests.
- Keep the values in the annotation or a short, well-named source method next to the test; no logic (conditions, loops) in the test body to handle the cases.
