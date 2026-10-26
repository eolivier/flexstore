import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonOrder, JsonOrderAddresses, JsonPaymentSession } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';
import { useCartStore } from '@/stores/cart.store';

// started: the customer opens the checkout page of the payment provider
export type PaymentStart =
  { status: 'started'; checkoutUrl: string } | { status: 'statusChanged' } | { status: 'error' };
export type OrderLookup = { status: 'found'; order: JsonOrder } | { status: 'notFound' } | { status: 'error' };
// refused: the account of the customer is suspended, locked or disabled
export type OrderPlacement =
  | { status: 'placed'; order: JsonOrder }
  | { status: 'emptyCart' }
  | { status: 'refused' }
  | { status: 'incompleteAddress' }
  | { status: 'outOfStock' }
  | { status: 'error' };

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

  // Places an order from the cart of the customer: it awaits its payment, the cart is emptied once it is paid
  // Without billing address, the order is billed to the delivery address
  async function placeOrder(addresses: JsonOrderAddresses): Promise<OrderPlacement> {
    try {
      const response = await authStore.authFetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(addresses),
      });
      if (response.status === 400) return { status: 'incompleteAddress' };
      if (response.status === 403) return { status: 'refused' };
      if (response.status === 409) return { status: 'outOfStock' };
      if (response.status === 422) return { status: 'emptyCart' };
      if (!response.ok) return { status: 'error' };
      const order: JsonOrder = await response.json();
      return { status: 'placed', order };
    } catch (error) {
      console.error('Failed to place the order', error);
      return { status: 'error' };
    }
  }

  // Starts the payment of an order: the customer then opens its checkout page
  async function startPayment(orderId: string): Promise<PaymentStart> {
    try {
      const response = await authStore.authFetch(`/api/orders/${encodeURIComponent(orderId)}/payment`, {
        method: 'POST',
      });
      // Paid, or cancelled because it was not paid in time
      if (response.status === 409) return { status: 'statusChanged' };
      if (!response.ok) return { status: 'error' };
      const paymentSession: JsonPaymentSession = await response.json();
      return { status: 'started', checkoutUrl: paymentSession.checkoutUrl };
    } catch (error) {
      console.error('Failed to start the payment', error);
      return { status: 'error' };
    }
  }

  // The cart is emptied once the order is paid
  async function refreshCart() {
    await cartStore.fetchCartItems();
  }

  return { orders, loadError, fetchOrders, fetchOrder, placeOrder, startPayment, refreshCart };
});
