import { defineStore } from 'pinia';
import { ref } from 'vue';
import type {
  JsonDiscountPreview,
  JsonOrder,
  JsonOrderAddresses,
  JsonPaymentSession,
  JsonShippingOptions,
} from '@/generated';
import { useAuthStore } from '@/stores/auth.store';
import { useCartStore } from '@/stores/cart.store';
import { pageQuery, totalCountOf } from '@/utils/pagination';

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
  // Refused for another reason, told by the shop (a field missing or malformed)
  | { status: 'invalidRequest'; reason: string }
  | { status: 'outOfStock' }
  // The terms of sale are not accepted, or were published again since the customer read them
  | { status: 'termsOfSaleNotAccepted' }
  | { status: 'outdatedTermsOfSale' }
  | { status: 'discountCodeRefused' }
  | { status: 'error' };

// What a discount code entered takes off the cart: applied, unknown, or no longer usable (expired, used up)
export type DiscountCheck =
  | { status: 'applied'; preview: JsonDiscountPreview }
  | { status: 'unknown' }
  | { status: 'unusable' }
  | { status: 'error' };

// The orders of the signed-in customer
export const useOrdersStore = defineStore('orders', () => {
  const orders = ref<JsonOrder[] | null>(null);
  // The page of the orders shown (from 0), and the number of all the orders of the customer
  const page = ref(0);
  const total = ref(0);
  const loadError = ref(false);
  const authStore = useAuthStore();
  const cartStore = useCartStore();

  async function fetchOrders(pageNumber: number = page.value) {
    loadError.value = false;
    try {
      const response = await authStore.authFetch(`/api/orders?${pageQuery(pageNumber)}`);
      if (response.ok) {
        const pageOrders: JsonOrder[] = await response.json();
        orders.value = pageOrders;
        page.value = pageNumber;
        total.value = totalCountOf(response, pageOrders.length);
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

  // A 400 tells an incomplete address or terms of sale not accepted only when the shop says so: any other reason
  // is shown as it is given
  function refusalOf(refusal: { reason?: string; error?: string }): OrderPlacement {
    if (refusal.reason === 'INCOMPLETE_ADDRESS') return { status: 'incompleteAddress' };
    if (refusal.reason === 'TERMS_OF_SALE_NOT_ACCEPTED') return { status: 'termsOfSaleNotAccepted' };
    return { status: 'invalidRequest', reason: refusal.error ?? '' };
  }

  // A 409 tells terms of sale published again when the shop says so, a lack of stock otherwise
  function conflictOf(conflict: { reason?: string }): OrderPlacement {
    if (conflict.reason === 'OUTDATED_TERMS_OF_SALE') return { status: 'outdatedTermsOfSale' };
    return { status: 'outOfStock' };
  }

  // Places an order from the cart of the customer: it awaits its payment, the cart is emptied once it is paid
  // Without billing address, the order is billed to the delivery address. The customer accepts the version of
  // the terms of sale they read
  async function placeOrder(addresses: JsonOrderAddresses): Promise<OrderPlacement> {
    try {
      const response = await authStore.authFetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(addresses),
      });
      if (response.status === 400) return refusalOf((await response.json().catch(() => null)) ?? {});
      if (response.status === 403) return { status: 'refused' };
      if (response.status === 409) return conflictOf((await response.json().catch(() => null)) ?? {});
      // The discount code is unknown, or no longer usable since it was applied
      if (response.status === 404 || response.status === 410) return { status: 'discountCodeRefused' };
      if (response.status === 422) return { status: 'emptyCart' };
      if (!response.ok) return { status: 'error' };
      const order: JsonOrder = await response.json();
      return { status: 'placed', order };
    } catch (error) {
      console.error('Failed to place the order', error);
      return { status: 'error' };
    }
  }

  // The shipping methods, with their cost for the cart of the customer; null when they cannot be read
  async function fetchShippingOptions(): Promise<JsonShippingOptions | null> {
    const response = await authStore.authFetch('/api/orders/shipping-options').catch(() => null);
    if (!response?.ok) return null;
    return response.json();
  }

  async function checkDiscountCode(code: string): Promise<DiscountCheck> {
    const query = new URLSearchParams({ code });
    const response = await authStore.authFetch(`/api/orders/discount-preview?${query}`).catch(() => null);
    if (response?.status === 404) return { status: 'unknown' };
    if (response?.status === 410) return { status: 'unusable' };
    if (!response?.ok) return { status: 'error' };
    return { status: 'applied', preview: await response.json() };
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

  return {
    orders,
    page,
    total,
    loadError,
    fetchOrders,
    fetchOrder,
    fetchShippingOptions,
    checkDiscountCode,
    placeOrder,
    startPayment,
    refreshCart,
  };
});
