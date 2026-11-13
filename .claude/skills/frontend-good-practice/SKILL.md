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
- **Same container for the same action**: the container follows the kind of action, and the same kind of action uses the same container everywhere (not a page here, a drawer or an inline edit there):
  - **An administration entity** (a user, a product) is created and edited in **its own page**, with the breadcrumbs, its own route (`/admin/users/:id/edit`, `/admin/create-product`) and a link from its list: its form is long or has uploads and translations, and the page can be linked to and reloaded. A new administration entity follows the same pattern.
  - **A quick edit** (an address, the shipment or the tracking of an order, the two-factor authentication) and **a confirmation** (a deletion, a refund: `ConfirmDialog`) are done in **a modal dialog** opened from the screen that shows the data, which stays visible behind.
  - **A setting of the shop** (the seller, the shop page content, the settings) is a page of its own, a single screen rather than the row of a list.
  - A new screen takes the container of the closest existing action of the same kind; a deliberate exception is written under "Choices to validate" (see below).
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
- **"Save" stays enabled even when no value has changed**: the user may want to save again (e.g. to republish the current values), and a disabled button does not say why. It is disabled only while the form is invalid, and shows a loading state while saving.
- **"Cancel" restores the initial values** (nested values included) and clears the save message, without leaving the screen. It is disabled while nothing differs from the initial values (the last saved ones), and while saving.
- **After a successful save**, the saved values become the new initial values: "Cancel" is disabled again.
- **Compare with the initial values, not with a "touched" flag**: a field edited then restored is not a modification, so "Cancel" is disabled again.
- Labels come from the i18n keys of the form (`save`, `cancel`), the same wording as the other forms ("Save" / "Cancel", "Enregistrer" / "Annuler").
- Test it: "Save" enabled and "Cancel" disabled at first, "Cancel" enabled after a change and disabled again when the initial value is typed back, "Cancel" restoring the initial values.

Reference implementation: `components/admin/seller/SellerForm.vue`. When another form needs the same logic, extract it into a composable (e.g. `useEditedForm`) rather than copying it.
