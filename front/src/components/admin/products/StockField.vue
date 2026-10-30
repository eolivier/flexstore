<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // The units of the product in stock, 0 or more
  const stock = defineModel<string>({ required: true });

  const { t } = useI18n();

  const stockRule = (v: string) => /^\d+$/.test(v) || t('stock.invalid');
</script>

<template>
  <v-text-field
    v-model="stock"
    :label="t('stock.units')"
    type="number"
    min="0"
    :rules="[stockRule]"
    prepend-inner-icon="mdi-package-variant"
    class="mt-2"
  />
</template>
