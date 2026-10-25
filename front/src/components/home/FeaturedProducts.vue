<script setup lang="ts">
  import { useStocksStore } from '@/stores/stocks.store';
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store';
  import type { Product } from '@/models/product';
  import { useAddToCart } from '@/composables/useAddToCart';
  import ProductCard from '@/components/products/ProductCard.vue';
  import CartSnackbar from '@/components/cart/CartSnackbar.vue';

  const FEATURED_COUNT = 4;

  const { t } = useI18n();
  const productsStore = useProductsStore();
  const stocksStore = useStocksStore();
  stocksStore.fetchStocks();
  const { addingProductId, feedback, addToCart } = useAddToCart();

  // Up to 4 products, taken in turn from each category so that the selection is varied
  const featuredProducts = computed(() => {
    const byCategory = new Map<string, Product[]>();
    for (const product of productsStore.products) {
      byCategory.set(product.category, [...(byCategory.get(product.category) ?? []), product]);
    }
    const queues = [...byCategory.values()];
    const featured: Product[] = [];
    while (featured.length < FEATURED_COUNT && queues.some((queue) => queue.length)) {
      for (const queue of queues) {
        const product = queue.shift();
        if (product && featured.length < FEATURED_COUNT) featured.push(product);
      }
    }
    return featured;
  });
</script>

<template>
  <section
    v-if="productsStore.loading || featuredProducts.length"
    class="mb-14"
    :aria-busy="productsStore.loading"
  >
    <div class="d-flex align-center mb-6">
      <h2 class="text-h5 font-weight-bold">{{ t('home.featuredTitle') }}</h2>
      <v-spacer />
      <v-btn to="/products/" variant="text" append-icon="mdi-arrow-right">{{ t('home.seeAll') }}</v-btn>
    </div>
    <v-row>
      <template v-if="productsStore.loading">
        <v-col v-for="index in FEATURED_COUNT" :key="index" cols="12" sm="6" lg="3">
          <v-skeleton-loader type="image, article" class="rounded-xl" />
        </v-col>
      </template>
      <v-col v-for="product in featuredProducts" v-else :key="product.id" cols="12" sm="6" lg="3">
        <ProductCard
          :product="product"
          :stock="stocksStore.quantityOf(product.id)"
          :adding="addingProductId === product.id"
          @add="addToCart"
        />
      </v-col>
    </v-row>
    <CartSnackbar v-model="feedback" />
  </section>
</template>
