import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth.store';

export interface ShopSettings {
  orderConfirmationEmail: 'ENABLED' | 'DISABLED';
  // The time a customer has to pay an order once placed; unpaid, it is cancelled
  paymentDelayMinutes: number;
  // The price of the items, in euros, from which the standard shipping is free (0: always free)
  freeShippingThreshold: number;
  // ENABLED once the setup assistant of the seller is finished; never changed by the administrators
  sellerSetup: 'ENABLED' | 'DISABLED';
  // The email that reminds a customer of the items left in their cart, sent once the cart was left for the delay
  abandonedCartReminder: 'ENABLED' | 'DISABLED';
  abandonedCartDelayHours: number;
}

// From 1 minute to 1 day
export const MAX_PAYMENT_DELAY_MINUTES = 24 * 60;

// From 1 hour to 30 days
export const MAX_ABANDONED_CART_DELAY_HOURS = 30 * 24;

// The settings of the shop, for the administrators
export const useSettingsStore = defineStore('settings', () => {
  const settings = ref<ShopSettings | null>(null);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchSettings() {
    loadError.value = false;
    const response = await authStore.authFetch('/api/admin/settings').catch(() => null);
    if (response?.ok) {
      settings.value = await response.json();
    } else {
      loadError.value = true;
    }
  }

  // Returns whether the change was saved
  async function changeOrderConfirmationEmail(action: 'enable' | 'disable'): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/admin/settings/order-confirmation-email/${action}`, { method: 'POST' })
      .catch(() => null);
    if (!response?.ok) return false;
    settings.value = await response.json();
    return true;
  }

  const enableOrderConfirmationEmail = () => changeOrderConfirmationEmail('enable');

  const disableOrderConfirmationEmail = () => changeOrderConfirmationEmail('disable');

  // Returns whether the change was saved
  async function changeAbandonedCartReminder(action: 'enable' | 'disable'): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/admin/settings/abandoned-cart-reminder/${action}`, { method: 'POST' })
      .catch(() => null);
    if (!response?.ok) return false;
    settings.value = await response.json();
    return true;
  }

  const enableAbandonedCartReminder = () => changeAbandonedCartReminder('enable');

  const disableAbandonedCartReminder = () => changeAbandonedCartReminder('disable');

  // Returns whether the change was saved
  async function changeAbandonedCartDelay(hours: number): Promise<boolean> {
    const response = await authStore
      .authFetch('/api/admin/settings/abandoned-cart-delay', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ hours }),
      })
      .catch(() => null);
    if (!response?.ok) return false;
    settings.value = await response.json();
    return true;
  }

  // Returns whether the change was saved
  async function changePaymentDelay(minutes: number): Promise<boolean> {
    const response = await authStore
      .authFetch('/api/admin/settings/payment-delay', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ minutes }),
      })
      .catch(() => null);
    if (!response?.ok) return false;
    settings.value = await response.json();
    return true;
  }

  // Returns whether the change was saved
  async function changeFreeShippingThreshold(amount: number): Promise<boolean> {
    const response = await authStore
      .authFetch('/api/admin/settings/free-shipping-threshold', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amount }),
      })
      .catch(() => null);
    if (!response?.ok) return false;
    settings.value = await response.json();
    return true;
  }

  return {
    settings,
    loadError,
    fetchSettings,
    enableOrderConfirmationEmail,
    disableOrderConfirmationEmail,
    enableAbandonedCartReminder,
    disableAbandonedCartReminder,
    changeAbandonedCartDelay,
    changePaymentDelay,
    changeFreeShippingThreshold,
  };
});
