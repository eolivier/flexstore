<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { stockStateOf } from '@/utils/stock';

  // Out of stock, a few units left, or in stock
  const props = defineProps<{ quantity: number }>();

  const { t } = useI18n();

  const badge = computed(() => {
    const stockState = stockStateOf(props.quantity);
    if (stockState === 'outOfStock') {
      return { color: 'error', icon: 'mdi-close-circle-outline', text: t('stock.outOfStock') };
    }
    if (stockState === 'fewLeft') {
      return { color: 'warning', icon: 'mdi-alert-outline', text: t('stock.fewLeft', props.quantity) };
    }
    return { color: 'success', icon: 'mdi-check-circle-outline', text: t('stock.inStock') };
  });
</script>

<template>
  <v-chip :color="badge.color" :prepend-icon="badge.icon" variant="tonal" size="small">{{ badge.text }}</v-chip>
</template>
