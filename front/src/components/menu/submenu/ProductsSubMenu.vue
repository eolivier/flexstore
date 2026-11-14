<script setup>
  import NavLinkItems from '../NavLinkItems.vue';
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
    <NavLinkItems :links="shopLinks" />
  </v-list-group>
</template>
