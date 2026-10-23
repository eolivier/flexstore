<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Product } from '@/models/product';
  import { categoryInfo } from './categories';
  import { formatPrice } from '@/utils/price';

  const props = defineProps<{ product: Product; adding?: boolean }>();
  defineEmits<{ (e: 'add', product: Product): void }>();

  const { t, locale } = useI18n();

  const category = computed(() => categoryInfo(props.product.category));
  const price = computed(() => formatPrice(props.product.price, props.product.currency, locale.value));
  const detailLink = computed(() => `/products/${props.product.id}`);
</script>

<template>
  <v-card class="product-card d-flex flex-column h-100">
    <router-link
      :to="detailLink"
      class="product-visual d-flex align-center justify-center"
      :style="{ background: category.tint }"
      :aria-label="product.name"
    >
      <v-icon :icon="category.icon" size="64" color="primary" class="product-icon" />
    </router-link>

    <v-card-text class="d-flex flex-column flex-grow-1 pa-5">
      <div class="text-caption text-secondary font-weight-bold text-uppercase mb-1">{{ t(category.label) }}</div>
      <h3 class="text-subtitle-1 font-weight-bold mb-1">
        <router-link :to="detailLink" class="product-link">{{ product.name }}</router-link>
      </h3>
      <p class="product-description text-body-2 text-medium-emphasis mb-4">{{ product.description }}</p>

      <div class="mt-auto">
        <div class="text-h6 font-weight-bold mb-3">{{ price }}</div>
        <v-btn
          color="primary"
          prepend-icon="mdi-cart-plus"
          block
          :loading="adding"
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
    transition: transform 0.15s ease, box-shadow 0.15s ease;
  }
  .product-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 24px rgba(31, 42, 68, 0.08);
  }
  .product-visual {
    aspect-ratio: 16 / 10;
    text-decoration: none;
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
