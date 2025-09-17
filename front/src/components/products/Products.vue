<script setup lang="ts">
import {onMounted} from "vue";
import {useProductsStore} from "../../stores/products.ts";
import type { Product } from "../../stores/products.ts";
import {useCartStore} from "../../stores/cart.ts";
import type { Item } from "../stores/cart";

const headers = [
  { title: 'Name', key: 'name' },
  { title: 'Description', key: 'description' },
  { title: 'Category', key: 'category' },
  { title: 'Price', key: 'price' },
  { title: 'Actions', key: 'actions', sortable: false },
];

const productsStore = useProductsStore();
const cartStore = useCartStore();

onMounted(() => {
  productsStore.fetchProducts();
});

function addToCart(product: Product) {
  const existingItem = cartStore.items.find(i => i.productId === product.id);
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
  };
}

</script>
<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <h1 class="text-h5 font-weight-bold mb-6">All products</h1>

        <v-data-table :headers="headers" :items="productsStore.products" class="elevation-2 text-left">
          <template #item.price="{ item }">
            <strong>{{ item.price }} {{ item.currency }}</strong>
          </template>
          <template #item.actions="{ item }">
            <v-btn color="primary" @click="addToCart(item)">
              Add to cart
            </v-btn>
          </template>
        </v-data-table>
      </v-container>
    </v-main>
  </v-app>
</template>
