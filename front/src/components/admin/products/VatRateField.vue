<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { VAT_RATES, type VatRate } from '@/models/product';

  // The rate of VAT the price of the product includes, with what it applies to
  const vatRate = defineModel<VatRate>({ required: true });

  const { t } = useI18n();

  const vatRates = computed(() =>
    VAT_RATES.map((rate) => ({
      title: t(`vatRate.${rate}.label`),
      subtitle: t(`vatRate.${rate}.examples`),
      value: rate,
    }))
  );
</script>

<template>
  <v-select
    v-model="vatRate"
    :items="vatRates"
    :label="t('vatRate.title')"
    :hint="t('vatRate.hint')"
    persistent-hint
  >
    <template #item="{ props: itemProps, item }">
      <v-list-item v-bind="itemProps" :subtitle="item.raw.subtitle" />
    </template>
  </v-select>
</template>
