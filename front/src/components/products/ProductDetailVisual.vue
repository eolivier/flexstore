<script setup lang="ts">
  import type { Product } from '@/models/product';
  import ProductVisual from './ProductVisual.vue';

  // The large picture of the product, on the tint of its category
  defineProps<{ product: Product; tint: string }>();
</script>

<template>
  <div class="detail-visual d-flex align-center justify-center" :style="{ background: tint }">
    <ProductVisual :product-id="product.id" :category="product.category" format="large" :icon-size="140" />
  </div>
</template>

<style scoped>
  .detail-visual {
    aspect-ratio: 4 / 3;
    border-radius: 24px;
    overflow: hidden;
  }
</style>
