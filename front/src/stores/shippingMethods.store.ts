import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonNewShippingMethod, JsonShippingMethod, JsonShippingMethodChange } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';
import { jsonRequest } from '@/utils/jsonRequest';

// invalid: a name missing or a negative cost; lastOne: at least one method must stay enabled
export type ShippingMethodSaving = 'saved' | 'invalid' | 'lastOne' | 'error';

// The shipping methods, for the administrators
export const useShippingMethodsStore = defineStore('shippingMethods', () => {
  const shippingMethods = ref<JsonShippingMethod[]>([]);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchShippingMethods() {
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch('/api/admin/shipping-methods').catch(() => null);
    if (response?.ok) {
      shippingMethods.value = await response.json();
    } else {
      loadError.value = true;
    }
    loading.value = false;
  }

  async function savedOutcomeOf(response: Response | null): Promise<ShippingMethodSaving> {
    if (response?.status === 400) return 'invalid';
    if (response?.status === 409) return 'lastOne';
    if (!response?.ok) return 'error';
    await fetchShippingMethods();
    return 'saved';
  }

  async function createShippingMethod(newShippingMethod: JsonNewShippingMethod): Promise<ShippingMethodSaving> {
    const response = await authStore
      .authFetch('/api/admin/shipping-methods', jsonRequest('POST', newShippingMethod))
      .catch(() => null);
    return savedOutcomeOf(response);
  }

  async function updateShippingMethod(
    id: string,
    shippingMethodChange: JsonShippingMethodChange
  ): Promise<ShippingMethodSaving> {
    const response = await authStore
      .authFetch(`/api/admin/shipping-methods/${encodeURIComponent(id)}`, jsonRequest('PUT', shippingMethodChange))
      .catch(() => null);
    return savedOutcomeOf(response);
  }

  return {
    shippingMethods,
    loading,
    loadError,
    fetchShippingMethods,
    createShippingMethod,
    updateShippingMethod,
  };
});
