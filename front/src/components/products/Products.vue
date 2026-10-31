<script setup lang="ts">
  import { useProductImagesStore } from '@/stores/productImages.store';
  import { useStocksStore } from '@/stores/stocks.store';
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter } from 'vue-router';
  import { useProductsStore } from '@/stores/products.store.ts';
  import ProductCard from './ProductCard.vue';
  import { useReviewsStore } from '@/stores/reviews.store';
  import CartSnackbar from '@/components/cart/CartSnackbar.vue';
  import { useAddToCart } from '@/composables/useAddToCart';
  import { categoryInfo, isKnownCategory } from './categories';
  import { shopLinks } from '@/components/menu/navigation';
  import { matchesCatalogSearch, type CatalogSearch } from '@/utils/productSearch';

  const props = defineProps<{ category?: string }>();

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  // The text searched, kept in the address (?q=...) so that a search can be shared and found back
  const search = computed(() => (typeof route.query.q === 'string' ? route.query.q : ''));

  // The name of a category in the language of the customer; none for a category without a label of its own
  function categoryName(category: string): string {
    return isKnownCategory(category) ? t(categoryInfo(category).label) : '';
  }

  const catalogSearch = computed<CatalogSearch>(() => ({ text: search.value, categoryName }));

  function clearSearch() {
    router.replace({ query: { ...route.query, q: undefined } });
  }

  const productsStore = useProductsStore();
  const stocksStore = useStocksStore();
  const reviewsStore = useReviewsStore();
  const { addingProductId, feedback, addToCart } = useAddToCart();

  const title = computed(() => t(props.category ? categoryInfo(props.category).label : 'nav.allProducts'));

  type SortKey = 'name' | 'priceAsc' | 'priceDesc';
  const sort = ref<SortKey>('name');
  const sortOptions = computed(() => [
    { title: t('products.sortName'), value: 'name' },
    { title: t('products.sortPriceAsc'), value: 'priceAsc' },
    { title: t('products.sortPriceDesc'), value: 'priceDesc' },
  ]);

  const products = computed(() => {
    const filtered = productsStore.products.filter((product) => {
      const inCategory = !props.category || product.category === props.category;
      return inCategory && matchesCatalogSearch(product, catalogSearch.value);
    });
    return filtered.sort((a, b) => {
      if (sort.value === 'priceAsc') return (a.price ?? 0) - (b.price ?? 0);
      if (sort.value === 'priceDesc') return (b.price ?? 0) - (a.price ?? 0);
      return (a.name ?? '').localeCompare(b.name ?? '');
    });
  });

  onMounted(() => {
    productsStore.fetchProducts();
    stocksStore.fetchStocks();
    reviewsStore.fetchRatings();
    useProductImagesStore().fetchProductsWithImage();
  });
</script>

<template>
  <v-container class="py-10">
    <div class="d-flex flex-wrap align-end ga-4 mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold">{{ title }}</h1>
        <p v-if="search" class="text-body-1 mt-1">{{ t('products.resultsFor', { search }) }}</p>
        <p v-if="!productsStore.loading && !productsStore.error" class="text-body-2 text-medium-emphasis mt-1">
          {{ t('products.count', products.length) }}
        </p>
      </div>
      <v-spacer />
      <v-select
        v-model="sort"
        :items="sortOptions"
        :label="t('products.sort')"
        density="compact"
        hide-details
        class="sort-select"
      />
    </div>

    <div class="d-flex flex-wrap ga-2 mb-8">
      <v-chip
        v-for="link in shopLinks"
        :key="link.to"
        :to="link.to"
        :prepend-icon="link.icon"
        :variant="route.path === link.to ? 'flat' : 'outlined'"
        color="primary"
      >
        {{ t(link.label) }}
      </v-chip>
    </div>

    <v-row v-if="productsStore.loading">
      <v-col v-for="n in 8" :key="n" cols="12" sm="6" md="4" lg="3">
        <v-skeleton-loader type="image, article" class="rounded-xl" />
      </v-col>
    </v-row>

    <v-alert v-else-if="productsStore.error" type="error" variant="tonal" rounded="lg">
      {{ t('products.error') }}
      <template #append>
        <v-btn variant="text" @click="productsStore.fetchProducts()">{{ t('products.retry') }}</v-btn>
      </template>
    </v-alert>

    <div v-else-if="products.length === 0" class="text-center py-16">
      <v-icon icon="mdi-package-variant" size="56" class="text-medium-emphasis mb-3" />
      <p class="text-body-1 text-medium-emphasis">
        {{ search ? t('products.noMatch', { search }) : t('products.empty') }}
      </p>
      <v-btn v-if="search" variant="tonal" color="primary" class="mt-4" @click="clearSearch">
        {{ t('products.clearSearch') }}
      </v-btn>
    </div>

    <v-row v-else>
      <v-col v-for="product in products" :key="product.id" cols="12" sm="6" md="4" lg="3">
        <ProductCard
          :product="product"
          :stock="stocksStore.quantityOf(product.id)"
          :rating="reviewsStore.ratingOf(product.id)"
          :adding="addingProductId === product.id"
          @add="addToCart"
        />
      </v-col>
    </v-row>

    <CartSnackbar v-model="feedback" />
  </v-container>
</template>

<style scoped>
  .sort-select {
    max-width: 220px;
    min-width: 200px;
  }
</style>
