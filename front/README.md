# flexstore – front end

Vue 3 + TypeScript shop built with Vite, Vuetify, Pinia, Vue Router and vue-i18n (FR/EN).

See the [main README](../README.md) for the setup, the scripts and the API.

## Structure

```
src/
  components/   # pages and components (products, cart, admin, layout, menu)
  composables/  # shared logic (add to cart)
  stores/       # Pinia stores (auth, cart, products, users)
  services/     # API calls through the generated client
  generated/    # TypeScript client generated from the OpenAPI spec (npm run gen:api)
  i18n/         # translations
  plugins/      # Vuetify theme and i18n setup
  utils/        # helpers (price formatting)
```
