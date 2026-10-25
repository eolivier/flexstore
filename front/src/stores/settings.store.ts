import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth.store';

export interface ShopSettings {
  orderConfirmationEmail: 'ENABLED' | 'DISABLED';
  // The time a customer has to pay an order once placed; unpaid, it is cancelled
  paymentDelayMinutes: number;
}

// From 1 minute to 1 day
export const MAX_PAYMENT_DELAY_MINUTES = 24 * 60;

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

  return {
    settings,
    loadError,
    fetchSettings,
    enableOrderConfirmationEmail,
    disableOrderConfirmationEmail,
    changePaymentDelay,
  };
});
