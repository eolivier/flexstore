<script setup>
  import { ref } from 'vue';
  import ProductsSubMenu from './submenu/ProductsSubMenu.vue';
  import AdminSubMenu from './submenu/AdminSubMenu.vue';
  import Language from '../Language.vue';

  const drawer = ref(false);
</script>

<template>
  <v-app-bar app color="primary" dark>
    <v-app-bar-nav-icon @click="drawer = !drawer" />
    <v-toolbar-title>
      Flexstore
    </v-toolbar-title>
    <Language />
  </v-app-bar>

  <v-navigation-drawer
    v-model="drawer"
    app
    width="260"
    class="overflow-hidden"
    :mobile-breakpoint="960"
    :temporary="$vuetify.display.smAndDown"
  >
    <v-list>
      <v-list-item to="/" prepend-icon="mdi-home">
        <v-list-item-title>Home</v-list-item-title>
      </v-list-item>

      <ProductsSubMenu />

      <v-list-item to="/cart" prepend-icon="mdi-cart">
        <v-list-item-title>Cart</v-list-item-title>
      </v-list-item>
      <v-list-item to="/orders" prepend-icon="mdi-truck">
        <v-list-item-title>Orders</v-list-item-title>
      </v-list-item>
      <v-list-item to="/user-profile" prepend-icon="mdi-account">
        <v-list-item-title>Profile</v-list-item-title>
      </v-list-item>

      <AdminSubMenu />

      <v-list-item to="/about" prepend-icon="mdi-information">
        <v-list-item-title>About</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>

  <v-main>
    <router-view />
  </v-main>
</template>
