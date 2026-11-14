<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Product } from '@/models/product';
  import { categoryInfo } from './categories';
  import StockBadge from './StockBadge.vue';
  import NotifyRestockButton from './NotifyRestockButton.vue';
  import ProductVisual from './ProductVisual.vue';
  import ProductPrice from './ProductPrice.vue';
  import AddedShine from './AddedShine.vue';
  import ProductRating from '@/components/reviews/ProductRating.vue';
  import type { Rating } from '@/stores/reviews.store';
  import WishlistToggleButton from './WishlistToggleButton.vue';
  import { productPath } from '@/utils/productSlug';

  // stock: the units of the product, null while unknown
  // rating: what the customers think of the product, null when nobody reviewed it
  const props = defineProps<{
    product: Product;
    stock?: number | null;
    adding?: boolean;
    rating?: Rating | null;
  }>();
  defineEmits<{ (e: 'add', product: Product): void }>();

  const { t } = useI18n();

  const category = computed(() => categoryInfo(props.product.category));
  const detailLink = computed(() => productPath(props.product));
</script>

<template>
  <v-card class="product-card d-flex flex-column h-100">
    <router-link
      :to="detailLink"
      class="product-visual d-flex align-center justify-center"
      :style="{ background: category.tint }"
      :aria-label="product.name"
    >
      <ProductVisual :product-id="product.id" :category="product.category" format="small" :icon-size="64" />
    </router-link>

    <v-card-text class="d-flex flex-column flex-grow-1 pa-5">
      <div class="text-caption text-secondary font-weight-bold text-uppercase mb-1">{{ t(category.label) }}</div>
      <h3 class="text-subtitle-1 font-weight-bold mb-1">
        <router-link :to="detailLink" class="product-link">{{ product.name }}</router-link>
      </h3>
      <ProductRating v-if="rating" :rating="rating" class="mb-1" />
      <p class="product-description text-body-2 text-medium-emphasis mb-4">{{ product.description }}</p>

      <div class="mt-auto">
        <div class="d-flex align-center justify-space-between flex-wrap ga-2 mb-3">
          <ProductPrice :product="product" />
          <StockBadge v-if="stock !== null && stock !== undefined" :quantity="stock" />
        </div>
        <v-btn
          color="primary"
          prepend-icon="mdi-cart-plus"
          block
          :loading="adding"
          :disabled="stock === 0"
          @click="$emit('add', product)"
        >
          {{ t('products.addToCart') }}
        </v-btn>
        <NotifyRestockButton v-if="stock === 0" :product-id="product.id" class="mt-2" />
      </div>
    </v-card-text>

    <WishlistToggleButton :product="product" class="wishlist-overlay" />
    <AddedShine :product-id="product.id" />
  </v-card>
</template>

<style scoped>
  .product-card {
    position: relative;
    transition:
      transform 0.15s ease,
      box-shadow 0.15s ease;
  }
  .wishlist-overlay {
    position: absolute;
    top: 10px;
    right: 10px;
  }
  .product-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 24px rgba(31, 42, 68, 0.08);
  }
  .product-visual {
    aspect-ratio: 16 / 10;
    text-decoration: none;
    overflow: hidden;
  }
  .product-link {
    color: inherit;
    text-decoration: none;
  }
  .product-link:hover {
    text-decoration: underline;
  }
  .product-icon {
    opacity: 0.85;
  }
  .product-description {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
</style>
