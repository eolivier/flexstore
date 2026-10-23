<script setup lang="ts">
  import NavigationMenu from './components/menu/NavigationMenu.vue';
  import AppFooter from './components/layout/AppFooter.vue';
</script>

<template>
  <v-app>
    <NavigationMenu />
    <v-main>
      <router-view />
    </v-main>
    <AppFooter />
  </v-app>
</template>
