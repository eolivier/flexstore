<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useProductsStore } from "@/stores/products.store.ts";
import { useCartStore } from "@/stores/cart.store.ts";
import type { Item } from "@/models/cart.ts";
import type { Product } from '@/models/product.ts';
import ProductCard from './ProductCard.vue';
import { categoryInfo } from './categories';
import { shopLinks } from '@/components/menu/navigation';

const props = defineProps<{ category?: string }>();

const { t } = useI18n();
const route = useRoute();

const productsStore = useProductsStore();
const cartStore = useCartStore();

const title = computed(() => t(props.category ? categoryInfo(props.category).label : 'nav.allProducts'));

type SortKey = 'name' | 'priceAsc' | 'priceDesc';
const sort = ref<SortKey>('name');
const sortOptions = computed(() => [
  { title: t('products.sortName'), value: 'name' },
  { title: t('products.sortPriceAsc'), value: 'priceAsc' },
  { title: t('products.sortPriceDesc'), value: 'priceDesc' },
]);

const products = computed(() => {
  const filtered = props.category
    ? productsStore.products.filter(product => product.category === props.category)
    : [...productsStore.products];
  return filtered.sort((a, b) => {
    if (sort.value === 'priceAsc') return (a.price ?? 0) - (b.price ?? 0);
    if (sort.value === 'priceDesc') return (b.price ?? 0) - (a.price ?? 0);
    return (a.name ?? '').localeCompare(b.name ?? '');
  });
});

const addingProductId = ref<string | undefined>();
const snackbar = ref({ show: false, success: true, text: '' });

onMounted(() => {
  productsStore.fetchProducts();
});

async function addToCart(product: Product) {
  addingProductId.value = product.id;
  const existingItem = cartStore.cartItems?.items.find(i => i.productId === product.id);
  const item = existingItem
    ? { ...existingItem, productQuantity: existingItem.productQuantity + 1 }
    : productToCartItem(product);
  const saved = await cartStore.saveItem(item);
  addingProductId.value = undefined;
  snackbar.value = {
    show: true,
    success: saved,
    text: saved ? t('products.added', { name: product.name }) : t('products.addError'),
  };
}

function productToCartItem(product: Product): Item {
  return {
    itemId: '',
    productId: product.id,
    productName: product.name,
    productDescription: product.description,
    productCategory: product.category,
    productPrice: product.price,
    productCurrency: product.currency,
    productQuantity: 1,
    itemPrice: product.price,
  };
}
</script>

<template>
  <v-container class="py-10">
    <div class="d-flex flex-wrap align-end ga-4 mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold">{{ title }}</h1>
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
      <p class="text-body-1 text-medium-emphasis">{{ t('products.empty') }}</p>
    </div>

    <v-row v-else>
      <v-col v-for="product in products" :key="product.id" cols="12" sm="6" md="4" lg="3">
        <ProductCard :product="product" :adding="addingProductId === product.id" @add="addToCart" />
      </v-col>
    </v-row>

    <v-snackbar v-model="snackbar.show" :color="snackbar.success ? 'primary' : 'error'" timeout="3000" location="bottom right">
      <v-icon :icon="snackbar.success ? 'mdi-check-circle-outline' : 'mdi-alert-circle-outline'" class="mr-2" />
      {{ snackbar.text }}
      <template v-if="snackbar.success" #actions>
        <v-btn to="/cart" variant="text" color="secondary">{{ t('products.viewCart') }}</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<style scoped>
  .sort-select {
    max-width: 220px;
    min-width: 200px;
  }
</style>
