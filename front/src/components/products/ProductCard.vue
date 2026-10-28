<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Product } from '@/models/product';
  import { categoryInfo } from './categories';
  import { formatPrice } from '@/utils/price';
  import { productTextIn } from '@/utils/productText';
  import StockBadge from './StockBadge.vue';
  import ProductVisual from './ProductVisual.vue';

  // stock: the units of the product, null while unknown
  const props = defineProps<{ product: Product; stock?: number | null; adding?: boolean }>();
  defineEmits<{ (e: 'add', product: Product): void }>();

  const { t, locale } = useI18n();

  const category = computed(() => categoryInfo(props.product.category));
  const price = computed(() => formatPrice(props.product.price, props.product.currency, locale.value));
  const detailLink = computed(() => `/products/${props.product.id}`);
  const text = computed(() => productTextIn(props.product, locale.value));
</script>

<template>
  <v-card class="product-card d-flex flex-column h-100">
    <router-link
      :to="detailLink"
      class="product-visual d-flex align-center justify-center"
      :style="{ background: category.tint }"
      :aria-label="text.name"
    >
      <ProductVisual :product-id="product.id" :category="product.category" format="small" :icon-size="64" />
    </router-link>

    <v-card-text class="d-flex flex-column flex-grow-1 pa-5">
      <div class="text-caption text-secondary font-weight-bold text-uppercase mb-1">{{ t(category.label) }}</div>
      <h3 class="text-subtitle-1 font-weight-bold mb-1">
        <router-link :to="detailLink" class="product-link">{{ text.name }}</router-link>
      </h3>
      <p class="product-description text-body-2 text-medium-emphasis mb-4">{{ text.description }}</p>

      <div class="mt-auto">
        <div class="d-flex align-center justify-space-between flex-wrap ga-2 mb-3">
          <span class="text-h6 font-weight-bold">{{ price }}</span>
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
      </div>
    </v-card-text>
  </v-card>
</template>

<style scoped>
  .product-card {
    transition:
      transform 0.15s ease,
      box-shadow 0.15s ease;
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
