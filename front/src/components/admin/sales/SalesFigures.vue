<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonSalesDashboard } from '@/generated';
  import { formatPrice } from '@/utils/price';
  import { averageOrderValue } from '@/utils/sales';

  // The revenue, the number of orders and the average basket of the period
  const props = defineProps<{ dashboard: JsonSalesDashboard }>();

  const { t, locale } = useI18n();

  const figures = computed(() => [
    {
      label: 'salesDashboard.revenue',
      icon: 'mdi-cash-multiple',
      value: formatPrice(props.dashboard.revenue, props.dashboard.currency, locale.value),
    },
    { label: 'salesDashboard.orders', icon: 'mdi-receipt-text-outline', value: `${props.dashboard.orderCount}` },
    {
      label: 'salesDashboard.averageBasket',
      icon: 'mdi-basket-outline',
      value: formatPrice(
        averageOrderValue(props.dashboard.revenue, props.dashboard.orderCount),
        props.dashboard.currency,
        locale.value
      ),
    },
  ]);
</script>

<template>
  <v-row>
    <v-col v-for="figure in figures" :key="figure.label" cols="12" md="4">
      <v-card class="pa-5 h-100" data-test="figure">
        <div class="d-flex align-center text-body-2 text-medium-emphasis mb-1">
          <v-icon :icon="figure.icon" size="18" class="mr-2" />
          {{ t(figure.label) }}
        </div>
        <div class="text-h4 font-weight-bold">{{ figure.value }}</div>
      </v-card>
    </v-col>
  </v-row>
</template>
