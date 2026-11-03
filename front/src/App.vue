<script setup lang="ts">
  import NavigationMenu from './components/menu/NavigationMenu.vue';
  import AppFooter from './components/layout/AppFooter.vue';
  import CookieBanner from './components/cookies/CookieBanner.vue';
</script>

<template>
  <v-app>
    <NavigationMenu />
    <v-main>
      <router-view />
    </v-main>
    <AppFooter />
    <CookieBanner />
  </v-app>
</template>
