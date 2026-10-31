import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonSalesDashboard } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';

// The numbers of days the sales can be shown over
export const SALES_PERIODS = [7, 30, 90] as const;
export type SalesPeriod = (typeof SALES_PERIODS)[number];

// The sales of the shop, for the administrators
export const useSalesDashboardStore = defineStore('salesDashboard', () => {
  const dashboard = ref<JsonSalesDashboard | null>(null);
  const days = ref<SalesPeriod>(30);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchDashboard(period: SalesPeriod = days.value) {
    days.value = period;
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch(`/api/admin/sales-dashboard?days=${period}`).catch(() => null);
    if (response?.ok) {
      dashboard.value = await response.json();
    } else {
      loadError.value = true;
    }
    loading.value = false;
  }

  return { dashboard, days, loading, loadError, fetchDashboard };
});
