<script setup lang="ts">
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useWishlistStore } from '@/stores/wishlist.store';
  import { useStocksStore } from '@/stores/stocks.store';
  import { useReviewsStore } from '@/stores/reviews.store';
  import { useAddToCart } from '@/composables/useAddToCart';
  import ProductCard from '@/components/products/ProductCard.vue';

  // The products the signed-in customer saved to their wishlist: unsaving one (the heart on its card) removes
  // it from this page
  const { t } = useI18n();
  const wishlistStore = useWishlistStore();
  const stocksStore = useStocksStore();
  const reviewsStore = useReviewsStore();
  const { addingProductId, addToCart } = useAddToCart();

  onMounted(() => {
    wishlistStore.fetchWishlist();
    stocksStore.fetchStocks();
    reviewsStore.fetchRatings();
  });
</script>

<template>
  <v-container class="py-10">
    <h1 class="text-h4 font-weight-bold">{{ t('wishlist.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mt-1 mb-6">{{ t('wishlist.subtitle') }}</p>

    <v-alert v-if="wishlistStore.loadError" type="error" variant="tonal" class="mb-4">
      {{ t('wishlist.loadError') }}
    </v-alert>

    <v-row v-if="wishlistStore.loading">
      <v-col v-for="n in 4" :key="n" cols="12" sm="6" md="4" lg="3">
        <v-skeleton-loader type="image, article" class="rounded-xl" />
      </v-col>
    </v-row>

    <v-card v-else-if="wishlistStore.products.length === 0" class="pa-10 text-center">
      <v-icon icon="mdi-heart-outline" size="48" class="text-medium-emphasis mb-3" />
      <p class="text-body-1 mb-4">{{ t('wishlist.empty') }}</p>
      <v-btn to="/products" color="primary">{{ t('products.backToShop') }}</v-btn>
    </v-card>

    <v-row v-else>
      <v-col v-for="product in wishlistStore.products" :key="product.id" cols="12" sm="6" md="4" lg="3">
        <ProductCard
          :product="product"
          :stock="stocksStore.quantityOf(product.id)"
          :rating="reviewsStore.ratingOf(product.id)"
          :adding="addingProductId === product.id"
          @add="addToCart"
        />
      </v-col>
    </v-row>

  </v-container>
</template>
