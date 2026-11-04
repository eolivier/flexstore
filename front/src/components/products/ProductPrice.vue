<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Product } from '@/models/product';
  import { formatPrice } from '@/utils/price';

  // The price to pay for a product; on promotion, the regular price is struck through next to it, with the
  // percentage off
  const props = defineProps<{ product: Product; priceClass?: string }>();

  const { t, locale } = useI18n();

  const isOnPromotion = computed(() => !!props.product.promotionPercentage);
  const currentPrice = computed(() =>
    formatPrice(props.product.currentPrice ?? props.product.price, props.product.currency, locale.value)
  );
  const regularPrice = computed(() => formatPrice(props.product.price, props.product.currency, locale.value));
</script>

<template>
  <div class="d-flex align-center flex-wrap ga-2">
    <span class="font-weight-bold" :class="[priceClass ?? 'text-h6', { 'text-error': isOnPromotion }]">{{
      currentPrice
    }}</span>
    <template v-if="isOnPromotion">
      <s class="text-body-2 text-medium-emphasis">{{ regularPrice }}</s>
      <v-chip color="error" size="small" label>
        {{ t('products.promotionBadge', { percentage: product.promotionPercentage }) }}
      </v-chip>
    </template>
  </div>
</template>
