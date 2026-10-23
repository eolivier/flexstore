<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { useProductsStore } from "@/stores/products.store.ts";
import { useCartStore } from "@/stores/cart.store.ts";
import type { Item } from "@/models/cart.ts";
import type { Product } from '@/models/product.ts';

const props = defineProps<{ category?: string }>();

const { t } = useI18n();

const titles: Record<string, string> = {
  CLOTHING: 'nav.clothing',
  ELECTRONICS: 'nav.electronics',
  ACCESSORIES: 'nav.accessories',
};

const title = computed(() => t(props.category ? titles[props.category] ?? 'nav.allProducts' : 'nav.allProducts'));

const headers = computed(() => [
  { title: t('table.name'), key: 'name' },
  { title: t('table.description'), key: 'description' },
  { title: t('table.category'), key: 'category' },
  { title: t('table.price'), key: 'price' },
  { title: t('table.actions'), key: 'actions', sortable: false },
]);

const productsStore = useProductsStore();
const cartStore = useCartStore();

const products = computed(() =>
  props.category
    ? productsStore.products.filter(product => product.category === props.category)
    : productsStore.products
);

onMounted(() => {
  productsStore.fetchProducts();
});

function addToCart(product: Product) {
  const existingItem = cartStore.cartItems?.items.find(i => i.productId === product.id);
  if (existingItem) {
    const updatedItem = { ...existingItem, productQuantity: existingItem.productQuantity + 1 };
    cartStore.saveItem(updatedItem);
  } else {
    cartStore.saveItem(productToCartItem(product));
  }
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
    productQuantity: 1, // valeur par défaut ou à adapter
    itemPrice: product.price,
  };
}

</script>
<template>
  <v-container class="py-10">
    <h1 class="text-h5 font-weight-bold mb-6">{{ title }}</h1>

    <v-data-table :headers="headers" :items="products" class="elevation-2 text-left">
      <template #item.price="{ item }">
        <strong>{{ item.price }} {{ item.currency }}</strong>
      </template>
      <template #item.actions="{ item }">
        <v-btn color="primary" @click="addToCart(item)">
          {{ t('products.addToCart') }}
        </v-btn>
      </template>
    </v-data-table>
  </v-container>
</template>
