<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Item } from '@/models/cart';
  import QuantityStepper from './QuantityStepper.vue';
  import { categoryInfo } from '@/components/products/categories';
  import { formatPrice } from '@/utils/price';

  const props = defineProps<{ item: Item }>();
  defineEmits<{ (e: 'update:quantity', quantity: number): void }>();

  const { t, locale } = useI18n();

  const category = computed(() => categoryInfo(props.item.productCategory));
  const unitPrice = computed(() =>
    formatPrice(props.item.productPrice, props.item.productCurrency, locale.value)
  );
  const detailLink = computed(() => `/products/${props.item.productId}`);
  const linePrice = computed(() => formatPrice(props.item.itemPrice, props.item.productCurrency, locale.value));
</script>

<template>
  <div class="cart-line d-flex align-center ga-4 py-4">
    <router-link
      :to="detailLink"
      class="line-visual d-flex align-center justify-center flex-shrink-0"
      :style="{ background: category.tint }"
      :aria-label="item.productName"
    >
      <v-icon :icon="category.icon" size="32" color="primary" />
    </router-link>

    <div class="flex-grow-1 line-info">
      <div class="text-caption text-secondary font-weight-bold text-uppercase">{{ t(category.label) }}</div>
      <router-link :to="detailLink" class="line-link text-subtitle-1 font-weight-bold">{{
        item.productName
      }}</router-link>
      <div class="text-body-2 text-medium-emphasis">{{ t('cart.unitPrice', { price: unitPrice }) }}</div>
    </div>

    <div class="line-actions d-flex align-center ga-4">
      <QuantityStepper
        :model-value="item.productQuantity"
        :min="1"
        @update:model-value="$emit('update:quantity', $event)"
      />
      <div class="line-price text-subtitle-1 font-weight-bold text-right">{{ linePrice }}</div>
      <v-btn
        icon="mdi-close"
        size="small"
        variant="text"
        :aria-label="t('cart.remove')"
        @click="$emit('update:quantity', 0)"
      />
    </div>
  </div>
</template>

<style scoped>
  .line-visual {
    text-decoration: none;
    width: 72px;
    height: 72px;
    border-radius: 14px;
  }
  .line-link {
    display: block;
    color: inherit;
    text-decoration: none;
  }
  .line-link:hover {
    text-decoration: underline;
  }
  .line-info {
    min-width: 0;
  }
  .line-price {
    min-width: 96px;
  }
  @media (max-width: 599px) {
    .cart-line {
      flex-wrap: wrap;
    }
    .line-actions {
      width: 100%;
      justify-content: space-between;
    }
  }
</style>
