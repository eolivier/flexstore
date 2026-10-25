import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonStock } from '@/generated';
import { useAuthStore } from './auth.store';
import { stocksApi } from '@/api';

// The stocks of the products: a product without stock has none
export const useStocksStore = defineStore('stocks', () => {
  const stocks = ref<Record<string, number>>({});
  const loaded = ref(false);
  const authStore = useAuthStore();

  async function fetchStocks() {
    const response = await stocksApi.getStocks().catch(() => null);
    if (!response) return;
    const jsonStocks: JsonStock[] = response.data;
    stocks.value = Object.fromEntries(jsonStocks.map((stock) => [stock.productId, stock.quantity]));
    loaded.value = true;
  }

  // The units of a product, or null while the stocks are not loaded (nothing is shown nor disabled)
  function quantityOf(productId: string | undefined): number | null {
    if (!loaded.value || productId === undefined) return null;
    return stocks.value[productId] ?? 0;
  }

  // For the administrators; returns whether the stock was changed
  async function changeStock(productId: string, quantity: number): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/admin/stocks/${encodeURIComponent(productId)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ quantity }),
      })
      .catch(() => null);
    if (!response?.ok) return false;
    const jsonStock: JsonStock = await response.json();
    stocks.value = { ...stocks.value, [jsonStock.productId]: jsonStock.quantity };
    return true;
  }

  return { stocks, loaded, fetchStocks, quantityOf, changeStock };
});
