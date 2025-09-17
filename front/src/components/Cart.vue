<script setup>
  import { onMounted } from 'vue';
  import { useCartStore } from '../stores/cart.ts';
  import { Item } from "../stores/cart.ts";

  const cartStore = useCartStore();

  onMounted(() => {
    cartStore.fetchItems();
  });

  const headers = [
    { title: 'Item id', key: 'itemId' },
    { title: 'Product name', key: 'productName' },
    { title: 'Product description', key: 'productDescription' },
    { title: 'Product quantity', key: 'productQuantity', align: 'end' },
  ];

  function incrementQuantityAndSave(item: Item) {
    item.productQuantity++;
    cartStore.saveItem(item);
  }

  function decrementQuantityAndSave(item: Item) {
    item.productQuantity--;
    if (item.productQuantity === 0) {
      cartStore.removeItem(item);
    } else {
      cartStore.saveItem(item);
    }
  }
</script>
<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <h1 class="text-h5 font-weight-bold mb-6">My Cart</h1>

        <v-data-table
          :headers="headers"
          :items="cartStore.items"
          class="elevation-2"
          hide-default-footer
        >
          <template #item.productQuantity="{ item }">
            <v-row align="center" no-gutters>
              <v-btn icon @click="decrementQuantityAndSave(item)">
                <v-icon>mdi-minus</v-icon>
              </v-btn>
              <v-text-field
                v-model="item.productQuantity"
                type="number"
                min="1"
                class="mx-2"
                style="width: 60px"
                hide-details
                dense
              />
              <v-btn icon @click="incrementQuantityAndSave(item)">
                <v-icon>mdi-plus</v-icon>
              </v-btn>
            </v-row>
          </template>
        </v-data-table>
      </v-container>
    </v-main>
  </v-app>
</template>
