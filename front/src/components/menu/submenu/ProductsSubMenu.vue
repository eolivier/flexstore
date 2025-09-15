<script setup>
  import { ref } from 'vue';

  const productsOpen = ref(false);
</script>
<template>
  <v-list-group v-model="productsOpen" prepend-icon="mdi-package-variant">
    <template #activator="{ props }">
      <v-list-item v-bind="props">Products</v-list-item>
    </template>
    <v-list-item to="/products/" prepend-icon="mdi-package-variant">
      <v-list-item-title>All products</v-list-item-title>
    </v-list-item>
    <v-list-item to="/products/clothing" prepend-icon="mdi-tshirt-crew">
      <v-list-item-title>Clothes</v-list-item-title>
    </v-list-item>
    <v-list-item to="/products/electronics" prepend-icon="mdi-cellphone">
      <v-list-item-title>Electronics</v-list-item-title>
    </v-list-item>
    <v-list-item to="/products/accessories" prepend-icon="mdi-watch">
      <v-list-item-title>Accessoiries</v-list-item-title>
    </v-list-item>
  </v-list-group>
</template>
