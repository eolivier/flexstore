<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // The shop, the category (label: its i18n key) and the name of the product
  defineProps<{ productName: string; categoryLabel: string; categoryLink: string }>();

  const { t } = useI18n();
</script>

<template>
  <v-breadcrumbs
    :items="[
      { title: t('products.shop'), to: '/products' },
      { title: t(categoryLabel), to: categoryLink },
      { title: productName, disabled: true },
    ]"
    class="px-0 pt-0 mb-4"
  >
    <template #divider><v-icon icon="mdi-chevron-right" size="16" /></template>
  </v-breadcrumbs>
</template>
