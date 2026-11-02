import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonLowStock, JsonStockAlert } from '@/generated';
import { useAuthStore } from './auth.store';

// The alerts on the stocks, for the administrators: the products whose stock reached their threshold, and the
// threshold of each product
export const useStockAlertsStore = defineStore('stockAlerts', () => {
  const lowStocks = ref<JsonLowStock[]>([]);
  const authStore = useAuthStore();

  // The low stocks are kept as they were when they cannot be read
  async function fetchLowStocks() {
    const response = await authStore.authFetch('/api/admin/stocks/low').catch(() => null);
    if (!response?.ok) return;
    lowStocks.value = await response.json();
  }

  // The threshold of a product, or null when it cannot be read
  async function thresholdOf(productId: string): Promise<number | null> {
    const response = await authStore
      .authFetch(`/api/admin/stocks/${encodeURIComponent(productId)}/alert`)
      .catch(() => null);
    if (!response?.ok) return null;
    const stockAlert: JsonStockAlert = await response.json();
    return stockAlert.threshold;
  }

  // Returns whether the threshold was changed
  async function changeThreshold(productId: string, threshold: number): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/admin/stocks/${encodeURIComponent(productId)}/alert`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ threshold }),
      })
      .catch(() => null);
    return response?.ok ?? false;
  }

  return { lowStocks, fetchLowStocks, thresholdOf, changeThreshold };
});
