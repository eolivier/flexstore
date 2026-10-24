---
name: front-good-practice
description: Good practices for the front end (Vue components, pages, stores, composables) - composition and fine-grained components to keep each component short. Use before creating, changing or reviewing a front-end component or page.
---

# Front-end good practices

Principles collected from the user's feedback. Apply them to the front-end code you write or change, and check them in reviews. When an existing component breaks them, point it out (and fix it if you are already changing that component).

## 1. Favour composition: fine-grained components

Build pages and components by composing small components, each with one responsibility, rather than writing long components that do everything. The goal is short components that can be read at a glance: as a guide, a component (script, template and style together) stays under about 80 lines; past 100 lines, look for the parts to extract.

Extract a child component when a part of the template:
- has a meaning of its own that can be named (`OrderLinesTable`, `OrderSummaryCard`, `OrderStatusStep`);
- is repeated (an item of a list: `v-for` over a child component);
- carries its own state, styles or behaviour.

Avoid: a page that loads the data, lays out the page, renders a table, a card per item and their styles in one file.

Prefer:

```
orders/
  Orders.vue              loads the orders, empty and error states, the list
  OrderSummaryCard.vue    one order of the list
  OrderDetail.vue         loads one order, not found and error states, the layout
  OrderStatusSteps.vue    the steps of the status and their connectors
  OrderStatusStep.vue     one step
  OrderLinesTable.vue     the lines and the total of an order
```

```vue
<!-- Orders.vue -->
<OrderSummaryCard v-for="order in ordersStore.orders" :key="order.id" :order="order" />
```

Details:
- **Pages orchestrate, children present**: the page (route component) loads the data through a store and handles the loading, empty and error states; the children receive what they display through props and report user actions through events (`update:quantity`), without calling the API themselves.
- **Logic shared by several components** goes in a composable (`useAddToCart`) or a utility module (`utils/order.ts`: number, date, item count, status steps), not copied in each component.
- **Group components by feature** (`components/orders/`, `components/cart/`, `components/products/`), next to the components they compose.
- Scoped styles follow the component that uses them: extracting a component also moves its styles, which keeps each style block short.
- Test the extracted components on their own (props in, rendered output and events out), and the logic of the utility modules with plain unit tests.
