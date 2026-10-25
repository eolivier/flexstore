<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useCartStore } from '@/stores/cart.store';
  import { formatPrice } from '@/utils/price';

  // The summary of the cart and the button that places the order; error: why the last attempt failed
  defineProps<{ placing: boolean; disabled: boolean; error: string | null }>();
  const emit = defineEmits<{ place: [] }>();

  const { t, locale } = useI18n();
  const cartStore = useCartStore();

  const itemsCount = computed(() =>
    (cartStore.cartItems?.items ?? []).reduce((count, item) => count + item.productQuantity, 0)
  );
  const total = computed(() =>
    formatPrice(cartStore.cartItems?.totalItemsPrice, cartStore.cartItems?.itemsCurrency, locale.value)
  );
</script>

<template>
  <v-card class="summary pa-6">
    <h2 class="text-h6 font-weight-bold mb-4">{{ t('cart.summary') }}</h2>
    <div class="text-body-2 text-medium-emphasis mb-2">{{ t('cart.itemsCount', itemsCount) }}</div>
    <v-divider class="my-4" />
    <div class="d-flex align-center mb-6">
      <span class="text-subtitle-1 font-weight-bold">{{ t('cart.total') }}</span>
      <v-spacer />
      <span class="text-h5 font-weight-bold">{{ total }}</span>
    </div>
    <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-4">{{
      t(`checkout.errors.${error}`)
    }}</v-alert>
    <v-btn
      color="secondary"
      size="large"
      block
      prepend-icon="mdi-check"
      :loading="placing"
      :disabled="disabled"
      @click="emit('place')"
    >
      {{ t('checkout.placeOrder') }}
    </v-btn>
  </v-card>
</template>

<style scoped>
  .summary {
    position: sticky;
    top: 96px;
  }
</style>
