<script setup lang="ts">
  import NavigationMenu from './components/menu/NavigationMenu.vue';
  import AppFooter from './components/layout/AppFooter.vue';
  import CookieBanner from './components/cookies/CookieBanner.vue';
  import GuidedTour from './components/guidedtour/GuidedTour.vue';
  import AppSnackbar from './components/layout/AppSnackbar.vue';
  import { useRouteHead } from './composables/useRouteHead';
  import { useShopPageContentHead } from './composables/useShopPageContentHead';

  useRouteHead();
  useShopPageContentHead();
</script>

<template>
  <v-app>
    <NavigationMenu />
    <v-main>
      <router-view />
    </v-main>
    <AppFooter />
    <CookieBanner />
    <GuidedTour />
    <AppSnackbar />
  </v-app>
</template>
