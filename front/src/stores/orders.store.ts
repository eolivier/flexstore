import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonOrder } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';
import { useCartStore } from '@/stores/cart.store';

export type OrderLookup = { status: 'found'; order: JsonOrder } | { status: 'notFound' } | { status: 'error' };
export type OrderPlacement = { status: 'placed'; order: JsonOrder } | { status: 'emptyCart' } | { status: 'error' };

// The orders of the signed-in customer
export const useOrdersStore = defineStore('orders', () => {
  const orders = ref<JsonOrder[] | null>(null);
  const loadError = ref(false);
  const authStore = useAuthStore();
  const cartStore = useCartStore();

  async function fetchOrders() {
    loadError.value = false;
    try {
      const response = await authStore.authFetch('/api/orders');
      if (response.ok) {
        orders.value = await response.json();
      } else {
        loadError.value = true;
      }
    } catch (error) {
      console.error('Failed to load the orders', error);
      loadError.value = true;
    }
  }

  // One order of the customer; the order of another customer is not found
  async function fetchOrder(orderId: string): Promise<OrderLookup> {
    try {
      const response = await authStore.authFetch(`/api/orders/${encodeURIComponent(orderId)}`);
      if (response.status === 404) return { status: 'notFound' };
      if (!response.ok) return { status: 'error' };
      return { status: 'found', order: await response.json() };
    } catch (error) {
      console.error('Failed to load the order', error);
      return { status: 'error' };
    }
  }

  // Places an order from the cart of the customer; the server empties the cart
  async function placeOrder(): Promise<OrderPlacement> {
    try {
      const response = await authStore.authFetch('/api/orders', { method: 'POST' });
      if (response.status === 422) return { status: 'emptyCart' };
      if (!response.ok) return { status: 'error' };
      const order: JsonOrder = await response.json();
      await cartStore.fetchCartItems();
      return { status: 'placed', order };
    } catch (error) {
      console.error('Failed to place the order', error);
      return { status: 'error' };
    }
  }

  return { orders, loadError, fetchOrders, fetchOrder, placeOrder };
});
