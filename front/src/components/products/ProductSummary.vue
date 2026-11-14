<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { Product } from '@/models/product';
  import type { Rating } from '@/stores/reviews.store';
  import ProductPrice from './ProductPrice.vue';
  import StockBadge from './StockBadge.vue';
  import WishlistToggleButton from './WishlistToggleButton.vue';
  import ProductRating from '@/components/reviews/ProductRating.vue';

  // What the customer reads about the product: category, name, rating, price, stock and description
  // stock: the units of the product, null while unknown
  defineProps<{
    product: Product;
    categoryLabel: string;
    categoryLink: string;
    stock: number | null;
    rating: Rating | null;
  }>();

  const { t } = useI18n();
</script>

<template>
  <router-link
    :to="categoryLink"
    class="category-link text-caption text-secondary font-weight-bold text-uppercase mb-2"
  >
    {{ t(categoryLabel) }}
  </router-link>
  <div class="d-flex align-center ga-3 mb-3">
    <h1 class="text-h4 font-weight-bold">{{ product.name }}</h1>
    <WishlistToggleButton :product="product" />
  </div>
  <ProductRating v-if="rating" :rating="rating" class="mb-3" />
  <div class="d-flex align-center flex-wrap ga-3 mb-5">
    <ProductPrice :product="product" price-class="text-h4" />
    <StockBadge v-if="stock !== null" :quantity="stock" />
  </div>
  <p class="text-body-1 text-medium-emphasis mb-8">{{ product.description }}</p>
</template>

<style scoped>
  .category-link {
    text-decoration: none;
    align-self: flex-start;
  }
</style>
