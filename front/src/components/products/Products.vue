<script setup>
import {onMounted} from "vue";
import {useProductsStore} from "../../stores/products.js";

const headers = [
  { title: 'Name', key: 'name' },
  { title: 'Description', key: 'description' },
  { title: 'Price', key: 'price' },
  { title: 'Currency', key: 'currency'},
  { title: 'Actions', key: 'actions', sortable: false },
];

const productsStore = useProductsStore();

onMounted(() => {
  productsStore.fetchProducts();
});

function addToCart(item) {
  // Ajoute le produit au panier ici
  console.log('Ajouté au panier:', item);
}

</script>
<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <h1 class="text-h5 font-weight-bold mb-6">All products</h1>

        <v-data-table :headers="headers" :items="productsStore.products" class="elevation-2 text-left">
          <template #item.price="{ item }">
            <strong>{{ item.price }}</strong>
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
