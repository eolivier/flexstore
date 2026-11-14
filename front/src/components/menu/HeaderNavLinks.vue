<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import NavLinkItems from './NavLinkItems.vue';
  import { shopLinks } from './navigation';

  const { t } = useI18n();
</script>

<template>
  <nav class="d-none d-md-flex align-center ga-1">
    <v-btn to="/" variant="text" exact>{{ t('nav.home') }}</v-btn>
    <v-menu open-on-hover location="bottom start">
      <template #activator="{ props }">
        <v-btn v-bind="props" variant="text" append-icon="mdi-chevron-down">
          {{ t('nav.shop') }}
        </v-btn>
      </template>
      <v-list density="comfortable" min-width="220">
        <NavLinkItems :links="shopLinks" />
      </v-list>
    </v-menu>
    <v-btn to="/about" variant="text">{{ t('nav.about') }}</v-btn>
  </nav>
</template>
