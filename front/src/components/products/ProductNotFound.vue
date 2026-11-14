<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // The product could not be loaded (failed: the customer can retry) or does not exist
  defineProps<{ failed: boolean }>();
  defineEmits<{ (e: 'retry'): void }>();

  const { t } = useI18n();
</script>

<template>
  <v-card class="pa-10 text-center">
    <v-icon
      :icon="failed ? 'mdi-alert-circle-outline' : 'mdi-package-variant-remove'"
      size="56"
      class="text-medium-emphasis mb-3"
    />
    <h1 class="text-h6 font-weight-bold mb-1">{{ failed ? t('products.loadError') : t('products.notFound') }}</h1>
    <p v-if="!failed" class="text-body-2 text-medium-emphasis mb-6">{{ t('products.notFoundText') }}</p>
    <div class="d-flex justify-center ga-2">
      <v-btn v-if="failed" variant="outlined" color="primary" @click="$emit('retry')">{{ t('retry') }}</v-btn>
      <v-btn to="/products" color="secondary" prepend-icon="mdi-arrow-left">{{ t('products.backToShop') }}</v-btn>
    </div>
  </v-card>
</template>
