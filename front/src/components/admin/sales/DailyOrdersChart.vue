<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonDailySales } from '@/generated';
  import { barHeightPercent } from '@/utils/sales';
  import { formatPrice } from '@/utils/price';

  // The orders of each day of the period, as bars
  const props = defineProps<{ days: JsonDailySales[]; currency: string }>();

  const { t, locale } = useI18n();

  const busiestDayOrderCount = computed(() => Math.max(0, ...props.days.map((day) => day.orderCount)));
  const bars = computed(() =>
    props.days.map((day) => ({
      day: day.day,
      height: barHeightPercent(day.orderCount, busiestDayOrderCount.value),
      label: t('salesDashboard.dayOrders', {
        day: dayOf(day),
        n: day.orderCount,
        amount: formatPrice(day.revenue, props.currency, locale.value),
      }),
    }))
  );

  function dayOf(day: JsonDailySales) {
    return new Date(`${day.day}T12:00:00`).toLocaleDateString(locale.value, { day: 'numeric', month: 'short' });
  }
</script>

<template>
  <v-card class="pa-5">
    <h2 class="text-h6 font-weight-bold mb-4">{{ t('salesDashboard.ordersPerDay') }}</h2>
    <div class="chart d-flex align-end" role="img" :aria-label="t('salesDashboard.ordersPerDay')">
      <div v-for="bar in bars" :key="bar.day" class="bar-slot d-flex align-end" data-test="bar-slot">
        <div class="bar bg-primary" data-test="bar" :style="{ height: `${bar.height}%` }" />
        <v-tooltip activator="parent" location="top" data-test="bar-tooltip">{{ bar.label }}</v-tooltip>
      </div>
    </div>
    <div class="d-flex justify-space-between text-caption text-medium-emphasis mt-2">
      <span>{{ dayOf(days[0]) }}</span>
      <span>{{ t('salesDashboard.busiestDay', { n: busiestDayOrderCount }) }}</span>
      <span>{{ dayOf(days[days.length - 1]) }}</span>
    </div>
  </v-card>
</template>

<style scoped>
  .chart {
    height: 180px;
    gap: 2px;
    border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
  }
  .bar-slot {
    flex: 1;
    height: 100%;
  }
  .bar {
    width: 100%;
    border-radius: 3px 3px 0 0;
  }
</style>
