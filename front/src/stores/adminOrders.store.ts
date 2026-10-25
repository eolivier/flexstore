import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonOrder } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';

// statusChanged: the order changed status in the meantime (another administrator), the orders are reloaded
export type StatusAdvance = 'advanced' | 'statusChanged' | 'error';

// The orders of all the customers, for the administrators
export const useAdminOrdersStore = defineStore('adminOrders', () => {
  const orders = ref<JsonOrder[]>([]);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchAllOrders() {
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch('/api/admin/orders').catch(() => null);
    if (response?.ok) {
      orders.value = await response.json();
    } else {
      loadError.value = true;
    }
    loading.value = false;
  }

  // Moves the order to the next status of its life cycle, from the status shown to the administrator
  async function advanceStatus(order: JsonOrder): Promise<StatusAdvance> {
    const response = await authStore
      .authFetch(`/api/admin/orders/${encodeURIComponent(order.id)}/status/advance`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentStatus: order.status }),
      })
      .catch(() => null);
    if (response?.status === 409) {
      await fetchAllOrders();
      return 'statusChanged';
    }
    if (!response?.ok) return 'error';
    const advancedOrder: JsonOrder = await response.json();
    orders.value = orders.value.map((stored) =>
      stored.id === advancedOrder.id ? advancedOrder : stored
    );
    return 'advanced';
  }

  return { orders, loading, loadError, fetchAllOrders, advanceStatus };
});
