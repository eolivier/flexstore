import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonAdminOrder, JsonOrder } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';
import { pageQuery, totalCountOf } from '@/utils/pagination';
import { hasTracking, NO_TRACKING, trackingToSend, type TrackingInput } from '@/utils/tracking';

// statusChanged: the order changed status in the meantime (another administrator), the orders are reloaded
export type StatusAdvance = 'advanced' | 'statusChanged' | 'error';

// notCorrectable: the order is not shipped or delivered (any more); invalid: the carrier or the number is
// refused
export type TrackingCorrection = 'corrected' | 'notCorrectable' | 'invalid' | 'error';

// refundRefused: the payment provider refused the refund, the order is unchanged
export type OrderRefundOutcome = 'refunded' | 'statusChanged' | 'refundRefused' | 'error';

// An order read by an administrator: found with its customer, unknown, or not readable
export type AdminOrderLookup =
  { status: 'found'; order: JsonAdminOrder } | { status: 'notFound' } | { status: 'error' };

// The orders of all the customers, with the customer of each one, for the administrators
export const useAdminOrdersStore = defineStore('adminOrders', () => {
  // A page of the orders (newest first): the page shown (from 0), and the number of all the orders
  const orders = ref<JsonAdminOrder[]>([]);
  const page = ref(0);
  const total = ref(0);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchAllOrders(pageNumber: number = page.value) {
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch(`/api/admin/orders?${pageQuery(pageNumber)}`).catch(() => null);
    if (response?.ok) {
      const pageOrders: JsonAdminOrder[] = await response.json();
      orders.value = pageOrders;
      page.value = pageNumber;
      total.value = totalCountOf(response, pageOrders.length);
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

  // Moves the order to the next status of its life cycle, from the status shown to the administrator, with the
  // tracking of the parcel when it is shipped; the order at its next status replaces it in the list
  async function advanceStatus(order: JsonOrder, tracking: TrackingInput = NO_TRACKING): Promise<StatusAdvance> {
    const response = await authStore
      .authFetch(`/api/admin/orders/${encodeURIComponent(order.id)}/status/advance`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          currentStatus: order.status,
          ...(hasTracking(tracking) ? trackingToSend(tracking) : {}),
        }),
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

  // Corrects the carrier and the tracking number of an order shipped or delivered
  async function correctTracking(order: JsonOrder, tracking: TrackingInput): Promise<TrackingCorrection> {
    const response = await authStore
      .authFetch(`/api/admin/orders/${encodeURIComponent(order.id)}/tracking`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(trackingToSend(tracking)),
      })
      .catch(() => null);
    if (response?.status === 409) return 'notCorrectable';
    if (response?.status === 400) return 'invalid';
    if (!response?.ok) return 'error';
    return 'corrected';
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

  return {
    orders, page, total, loading, loadError, fetchAllOrders, fetchOrder,
    advanceStatus, correctTracking, refundOrder,
  };
});
