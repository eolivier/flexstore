import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonDiscountCode, JsonNewDiscountCode } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';

// alreadyExists: another discount code has this code; invalid: a code, reduction or usage limit refused
export type DiscountCodeCreation = 'created' | 'alreadyExists' | 'invalid' | 'error';

// The discount codes, for the administrators
export const useDiscountCodesStore = defineStore('discountCodes', () => {
  const discountCodes = ref<JsonDiscountCode[]>([]);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchDiscountCodes() {
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch('/api/admin/discount-codes').catch(() => null);
    if (response?.ok) {
      discountCodes.value = await response.json();
    } else {
      loadError.value = true;
    }
    loading.value = false;
  }

  async function createDiscountCode(newDiscountCode: JsonNewDiscountCode): Promise<DiscountCodeCreation> {
    const response = await authStore
      .authFetch('/api/admin/discount-codes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newDiscountCode),
      })
      .catch(() => null);
    if (response?.status === 409) return 'alreadyExists';
    if (response?.status === 400) return 'invalid';
    if (!response?.ok) return 'error';
    await fetchDiscountCodes();
    return 'created';
  }

  // Returns whether the code was deleted
  async function deleteDiscountCode(code: string): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/admin/discount-codes/${encodeURIComponent(code)}`, { method: 'DELETE' })
      .catch(() => null);
    if (!response?.ok) return false;
    discountCodes.value = discountCodes.value.filter((discountCode) => discountCode.code !== code);
    return true;
  }

  return { discountCodes, loading, loadError, fetchDiscountCodes, createDiscountCode, deleteDiscountCode };
});
