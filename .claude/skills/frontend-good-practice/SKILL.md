---
name: frontend-good-practice
description: Good practices for the front end (Vue components, pages, stores, composables) - composition and fine-grained components to keep each component short, homogeneous UX choices across the application (same pattern for the same interaction). Use before creating, changing or reviewing a front-end component or page.
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

## 2. Keep the UX choices homogeneous

The same interaction is built the same way everywhere in the application. A user who has learned how one screen works must find the same behaviour on the others.

Rules:
- **Same container for the same action**: if a user is edited in a modal dialog, a product is edited in a modal dialog too (not in a page, a drawer or inline). The same goes for creation, deletion confirmation, details, and so on.
- **Same buttons, same order, same wording**: if a form has a "Save" and a "Cancel" button, every form has them, in the same place and the same order, with the same labels (through the shared i18n keys), the same colours and the same variants.
- **Same behaviour after an action**: if the modal closes when the form is saved, it closes everywhere; if the list is refreshed, a success message is displayed or the error is shown in the form after a save, it is done the same way everywhere. Cancel, the close icon and the Escape key behave the same way too.
- **Same feedback**: loading states, validation errors, empty states, success and error messages follow one pattern.

How to apply:
- **Before building a screen**, look at how the existing screens handle the same interaction (an edit, a creation, a deletion, a list, a filter) and reproduce it. Do not invent a new pattern.
- **Share rather than copy**: when two screens do the same thing, extract the shared piece (a generic `EditDialog`, a `FormActions` component with Save and Cancel, a composable for the save-then-close flow) so that the homogeneity is guaranteed by the code, not by discipline.
- **When a screen differs from the others**, point it out in the review and align it (in its own refactoring pull request when you are not already changing it).

Exceptions: an exception is allowed when following the common pattern would harm the user experience (e.g. a long form of many fields that does not fit in a modal, a multi-step editing flow, an action that must stay visible while the user scrolls, an inline edit of a single value). The exception stays rare, is deliberate and consistent with the other screens of the same kind, and is written in the description of the pull request under "Choices to validate" with the reason.

## 3. Save and Cancel in an edit form

An editable form (settings, seller, product, user...) offers a "Cancel" and a "Save" button, built the same way everywhere:
- **Placement**: the two buttons form one group, aligned to the right of the form (of the modal dialog when the form is in one: `<div class="d-flex justify-end ga-2">`). "Cancel" comes first, on the left, as a text button (`variant="text"`); "Save" comes last, on the right, as the filled primary button (`type="submit" color="secondary"`). The main action is the rightmost one, as in the Material Design convention that Vuetify follows.
- **"Save" is disabled by default**: it is enabled only when at least one editable value differs from its initial value (the last saved one). Typing the initial value back disables it again. It is also disabled while the form is invalid, and shows a loading state while saving.
- **"Cancel" restores the initial values** (nested values included) and clears the save message, without leaving the screen. It is disabled while nothing is modified, and while saving.
- **After a successful save**, the saved values become the new initial values: both buttons are disabled again.
- **Compare with the initial values, not with a "touched" flag**: a field edited then restored is not a modification.
- Labels come from the i18n keys of the form (`save`, `cancel`), the same wording as the other forms ("Save" / "Cancel", "Enregistrer" / "Annuler").
- Test it: both buttons disabled at first, enabled after a change, Save disabled again when the initial value is typed back, Cancel restoring the initial values.

Reference implementation: `components/admin/seller/SellerForm.vue`. When another form needs the same logic, extract it into a composable (e.g. `useEditedForm`) rather than copying it.
