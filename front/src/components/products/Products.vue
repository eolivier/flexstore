<script setup lang="ts">
  import EmptyState from '@/components/layout/EmptyState.vue';
  import LoadError from '@/components/layout/LoadError.vue';
  import { useProductImagesStore } from '@/stores/productImages.store';
  import { useStocksStore } from '@/stores/stocks.store';
  import { computed, onMounted, onServerPrefetch, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter } from 'vue-router';
  import { useProductsStore } from '@/stores/products.store.ts';
  import ProductCard from './ProductCard.vue';
  import { useReviewsStore } from '@/stores/reviews.store';
  import { useWishlistStore } from '@/stores/wishlist.store';
  import { useAuthStore } from '@/stores/auth.store';
  import { useAddToCart } from '@/composables/useAddToCart';
  import { categoryInfo, isKnownCategory } from './categories';
  import { shopLinks } from '@/components/menu/navigation';
  import { matchesCatalogSearch, type CatalogSearch } from '@/utils/productSearch';
  import { usePageHead } from '@/composables/usePageHead';
  import { withoutLanguage } from '@/utils/language';

  const props = defineProps<{ category?: string }>();

  // The catalog page of a link: its address is the one of the page, whatever its language or final slash
  function isShown(linkPath: string): boolean {
    return withoutLanguage(route.path).replace(/\/+$/, '') === linkPath;
  }

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
  const wishlistStore = useWishlistStore();
  const authStore = useAuthStore();
  const { addingProductId, addToCart } = useAddToCart();

  const title = computed(() => t(props.category ? categoryInfo(props.category).label : 'nav.allProducts'));

  // A search result is not a page to list: the catalog page it comes from is
  usePageHead(() => ({
    title: title.value,
    description: t(categoryInfo(props.category).description),
    path: route.path,
    indexed: search.value === '',
    translated: true,
    imageUrl: null,
    structuredData: null,
  }));

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

  // Pre-rendering waits for the products: the page is then listed with them
  onServerPrefetch(() => productsStore.fetchProducts());

  onMounted(() => {
    productsStore.fetchProducts();
    stocksStore.fetchStocks();
    reviewsStore.fetchRatings();
    useProductImagesStore().fetchProductsWithImage();
    if (authStore.isAuthenticated) wishlistStore.fetchWishlist();
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
        :variant="isShown(link.to) ? 'flat' : 'outlined'"
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

    <LoadError
      v-else-if="productsStore.error"
      :message="t('products.loadError')"
      retryable
      @retry="productsStore.fetchProducts()"
    />

    <EmptyState
      v-else-if="products.length === 0"
      icon="mdi-package-variant"
      :message="search ? t('products.noMatch', { search }) : t('products.empty')"
    >
      <v-btn v-if="search" variant="tonal" color="primary" @click="clearSearch">
        {{ t('products.clearSearch') }}
      </v-btn>
    </EmptyState>

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

  </v-container>
</template>

<style scoped>
  .sort-select {
    max-width: 220px;
    min-width: 200px;
  }
</style>
