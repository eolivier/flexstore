<script setup>
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { shopLinks } from '../navigation';

  const { t } = useI18n();
  const productsOpen = ref(false);
</script>
<template>
  <v-list-group v-model="productsOpen" value="products">
    <template #activator="{ props }">
      <v-list-item v-bind="props" prepend-icon="mdi-storefront-outline" :title="t('nav.shop')" />
    </template>
    <v-list-item
      v-for="link in shopLinks"
      :key="link.to"
      :to="link.to"
      :prepend-icon="link.icon"
      :title="t(link.label)"
    />
  </v-list-group>
</template>
