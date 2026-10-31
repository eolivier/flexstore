<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { SALES_PERIODS, useSalesDashboardStore, type SalesPeriod } from '@/stores/salesDashboard.store';
  import BestSellersTable from './BestSellersTable.vue';
  import DailyOrdersChart from './DailyOrdersChart.vue';
  import SalesFigures from './SalesFigures.vue';

  // The sales of the shop over the last 7, 30 or 90 days: revenue, orders per day and best sellers
  const salesDashboardStore = useSalesDashboardStore();
  const { t } = useI18n();

  onMounted(() => salesDashboardStore.fetchDashboard());

  function choose(period: SalesPeriod | undefined) {
    if (period) salesDashboardStore.fetchDashboard(period);
  }
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <div class="d-flex align-center justify-space-between flex-wrap ga-3 mb-2">
      <h1 class="text-h4 font-weight-bold">{{ t('salesDashboard.title') }}</h1>
      <v-btn-toggle
        :model-value="salesDashboardStore.days"
        mandatory
        density="comfortable"
        color="primary"
        variant="outlined"
        @update:model-value="choose"
      >
        <v-btn v-for="period in SALES_PERIODS" :key="period" :value="period">
          {{ t('salesDashboard.lastDays', { n: period }) }}
        </v-btn>
      </v-btn-toggle>
    </div>
    <p class="text-body-2 text-medium-emphasis mb-6">{{ t('salesDashboard.subtitle') }}</p>

    <v-alert v-if="salesDashboardStore.loadError" type="error" variant="tonal">
      {{ t('salesDashboard.error') }}
    </v-alert>
    <v-skeleton-loader v-else-if="!salesDashboardStore.dashboard" type="card, card" />
    <div v-else class="d-flex flex-column ga-6" :class="{ 'opacity-60': salesDashboardStore.loading }">
      <SalesFigures :dashboard="salesDashboardStore.dashboard" />
      <DailyOrdersChart :days="salesDashboardStore.dashboard.days" />
      <BestSellersTable
        :best-sellers="salesDashboardStore.dashboard.bestSellers"
        :currency="salesDashboardStore.dashboard.currency"
      />
    </div>
  </v-container>
</template>
