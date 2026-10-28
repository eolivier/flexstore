import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonAdminOrder, JsonOrder } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';

// statusChanged: the order changed status in the meantime (another administrator), the orders are reloaded
export type StatusAdvance = 'advanced' | 'statusChanged' | 'error';

// refundRefused: the payment provider refused the refund, the order is unchanged
export type OrderRefundOutcome = 'refunded' | 'statusChanged' | 'refundRefused' | 'error';

// An order read by an administrator: found with its customer, unknown, or not readable
export type AdminOrderLookup =
  { status: 'found'; order: JsonAdminOrder } | { status: 'notFound' } | { status: 'error' };

// The orders of all the customers, with the customer of each one, for the administrators
export const useAdminOrdersStore = defineStore('adminOrders', () => {
  const orders = ref<JsonAdminOrder[]>([]);
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

  async function fetchOrder(id: string): Promise<AdminOrderLookup> {
    const response = await authStore.authFetch(`/api/admin/orders/${encodeURIComponent(id)}`).catch(() => null);
    if (response?.status === 404) return { status: 'notFound' };
    if (!response?.ok) return { status: 'error' };
    return { status: 'found', order: await response.json() };
  }

  // Moves the order to the next status of its life cycle, from the status shown to the administrator; the order
  // at its next status replaces it in the list
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
    const advancedOrder: JsonAdminOrder = await response.json();
    orders.value = orders.value.map((stored) => (stored.id === advancedOrder.id ? advancedOrder : stored));
    return 'advanced';
  }

  // Cancels and refunds the order, from the status shown to the administrator
  async function refundOrder(order: JsonOrder): Promise<OrderRefundOutcome> {
    const response = await authStore
      .authFetch(`/api/admin/orders/${encodeURIComponent(order.id)}/refund`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentStatus: order.status }),
      })
      .catch(() => null);
    if (response?.status === 409) return 'statusChanged';
    if (response?.status === 502) return 'refundRefused';
    if (!response?.ok) return 'error';
    return 'refunded';
  }

  return { orders, loading, loadError, fetchAllOrders, fetchOrder, advanceStatus, refundOrder };
});
