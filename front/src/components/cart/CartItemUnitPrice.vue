<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Item } from '@/models/cart';
  import { formatPrice } from '@/utils/price';

  // The price of one product of the cart; on promotion, the regular price is struck through next to it, with
  // the percentage off
  const props = defineProps<{ item: Item }>();

  const { t, locale } = useI18n();

  const isOnPromotion = computed(() => !!props.item.promotionPercentage);
  const unitPrice = computed(() =>
    formatPrice(props.item.productPrice, props.item.productCurrency, locale.value)
  );
  const regularPrice = computed(() =>
    formatPrice(props.item.regularPrice, props.item.productCurrency, locale.value)
  );
</script>

<template>
  <div class="d-flex align-center flex-wrap ga-2 text-body-2 text-medium-emphasis">
    <span :class="{ 'text-error': isOnPromotion }">{{ t('cart.unitPrice', { price: unitPrice }) }}</span>
    <template v-if="isOnPromotion">
      <s>{{ regularPrice }}</s>
      <v-chip color="error" size="x-small" label>
        {{ t('products.promotionBadge', { percentage: item.promotionPercentage }) }}
      </v-chip>
    </template>
  </div>
</template>
