<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useCartStore } from '@/stores/cart.store';
  import { formatPrice } from '@/utils/price';

  // The summary of the cart, with the shipping chosen (null while it is not known), and the button that places
  // the order; error: why the last attempt failed, as told to the customer
  const props = defineProps<{
    shippingCost: number | null;
    // What the discount code applied takes off the items (0 without code)
    discountAmount: number;
    placing: boolean;
    disabled: boolean;
    error: string | null;
  }>();
  const emit = defineEmits<{ place: [] }>();

  const { t, locale } = useI18n();
  const cartStore = useCartStore();

  const itemsCount = computed(() =>
    (cartStore.cartItems?.items ?? []).reduce((count, item) => count + item.productQuantity, 0)
  );
  const currency = computed(() => cartStore.cartItems?.itemsCurrency);
  const itemsPrice = computed(() => cartStore.cartItems?.totalItemsPrice ?? 0);
  const shipping = computed(() => {
    if (props.shippingCost === null) return '';
    if (props.shippingCost === 0) return t('checkout.shipping.free');
    return formatPrice(props.shippingCost, currency.value, locale.value);
  });
  const total = computed(() =>
    formatPrice(
      itemsPrice.value - props.discountAmount + (props.shippingCost ?? 0),
      currency.value,
      locale.value
    )
  );
</script>

<template>
  <v-card class="summary pa-6">
    <h2 class="text-h6 font-weight-bold mb-4">{{ t('cart.summary') }}</h2>
    <div class="d-flex text-body-2 mb-2">
      <span class="text-medium-emphasis">{{ t('cart.itemsCount', itemsCount) }}</span>
      <v-spacer />
      <span>{{ formatPrice(itemsPrice, currency, locale) }}</span>
    </div>
    <div v-if="discountAmount > 0" class="d-flex text-body-2 mb-2">
      <span class="text-medium-emphasis">{{ t('checkout.discount.title') }}</span>
      <v-spacer />
      <span class="text-success">−{{ formatPrice(discountAmount, currency, locale) }}</span>
    </div>
    <div v-if="shipping" class="d-flex text-body-2 mb-2">
      <span class="text-medium-emphasis">{{ t('checkout.shipping.title') }}</span>
      <v-spacer />
      <span>{{ shipping }}</span>
    </div>
    <v-divider class="my-4" />
    <div class="d-flex align-center mb-6">
      <span class="text-subtitle-1 font-weight-bold">{{ t('cart.total') }}</span>
      <v-spacer />
      <span class="text-h5 font-weight-bold">{{ total }}</span>
    </div>
    <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-4">{{ error }}</v-alert>
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
