<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';

  // Out of stock, a few units left (5 at most), or in stock
  const props = defineProps<{ quantity: number }>();

  const { t } = useI18n();

  const FEW_UNITS = 5;

  const badge = computed(() => {
    if (props.quantity === 0)
      return { color: 'error', icon: 'mdi-close-circle-outline', text: t('stock.outOfStock') };
    if (props.quantity <= FEW_UNITS) {
      return { color: 'warning', icon: 'mdi-alert-outline', text: t('stock.fewLeft', props.quantity) };
    }
    return { color: 'success', icon: 'mdi-check-circle-outline', text: t('stock.inStock') };
  });
</script>

<template>
  <v-chip :color="badge.color" :prepend-icon="badge.icon" variant="tonal" size="small">{{ badge.text }}</v-chip>
</template>
