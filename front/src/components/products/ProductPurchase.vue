<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import QuantityStepper from '@/components/cart/QuantityStepper.vue';
  import NotifyRestockButton from './NotifyRestockButton.vue';

  // The quantity to order and the button that adds it to the cart, with what the cart already holds
  // stock: the units of the product, null while unknown
  defineProps<{ productId: string | undefined; stock: number | null; adding: boolean; quantityInCart: number }>();
  defineEmits<{ (e: 'add'): void }>();
  const quantity = defineModel<number>({ required: true });

  const { t } = useI18n();
</script>

<template>
  <div class="d-flex flex-wrap align-center ga-4 mb-3">
    <div>
      <div class="text-caption text-medium-emphasis mb-1">{{ t('products.quantity') }}</div>
      <QuantityStepper v-model="quantity" :min="1" />
    </div>
    <v-btn
      color="primary"
      size="large"
      prepend-icon="mdi-cart-plus"
      class="flex-grow-1 align-self-end"
      :loading="adding"
      :disabled="stock === 0"
      @click="$emit('add')"
    >
      {{ t('products.addToCart') }}
    </v-btn>
  </div>

  <NotifyRestockButton v-if="stock === 0" :product-id="productId" class="mb-3" />

  <div class="text-body-2 text-medium-emphasis mb-8">
    <v-icon icon="mdi-cart-outline" size="16" class="mr-1" />
    {{ t('products.inCart', quantityInCart) }}
    <router-link v-if="quantityInCart > 0" to="/cart" class="text-secondary ml-1">{{
      t('products.viewCart')
    }}</router-link>
  </div>
</template>
