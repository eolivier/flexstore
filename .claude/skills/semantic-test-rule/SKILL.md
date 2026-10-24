---
name: semantic-test-rule
description: Rules for the structure and wording of automated tests (unit, integration, front-end). Use before writing, changing or reviewing any test, in any language or framework (JUnit, Kotest, Vitest...).
---

# Semantic test rules

Rules collected from the user's feedback. Apply them to every test you write or change, and check them in reviews. When an existing test breaks them, point it out (and fix it if you are already changing that test).

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
- Keep each section once and in order; do not interleave several Act and Assert blocks. A test that needs two actions to check two behaviours is two tests.
- When the action is expected to fail, capture the failure in Act and check it in Assert, so the sections stay separate:

  ```kotlin
  // Act
  val thrown = catchThrowable { cart.add(product, Quantity(0)) }
  // Assert
  assertThat(thrown).isInstanceOf(InvalidQuantityException::class.java)
  ```

- Setup shared by all the tests of a class (fields, `@BeforeEach`, `beforeEach`) is part of Arrange; a test with nothing more to arrange may omit its `// Arrange` section, but keeps `// Act` and `// Assert`.
