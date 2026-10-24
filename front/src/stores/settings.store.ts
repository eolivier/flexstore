import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth.store';

export interface ShopSettings {
  orderConfirmationEmail: 'ENABLED' | 'DISABLED';
}

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

  return { settings, loadError, fetchSettings, enableOrderConfirmationEmail, disableOrderConfirmationEmail };
});
