import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth.store';

// The seller written on the invoices: the address on one line, the SIRET (14 digits) and the intra-community VAT
// number of the shop
export interface Seller {
  name: string;
  address: string;
  siret: string;
  vatNumber: string;
}

// saved; invalid: refused by the shop (a malformed SIRET or VAT number); failed: not reached
export type SellerUpdate = 'saved' | 'invalid' | 'failed';

// The seller of the shop, for the administrators: the invoices already issued keep the seller they were
// issued with
export const useSellerStore = defineStore('seller', () => {
  const seller = ref<Seller | null>(null);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchSeller() {
    loadError.value = false;
    const response = await authStore.authFetch('/api/admin/seller').catch(() => null);
    if (response?.ok) {
      seller.value = await response.json();
    } else {
      loadError.value = true;
    }
  }

  async function updateSeller(editedSeller: Seller): Promise<SellerUpdate> {
    const response = await authStore
      .authFetch('/api/admin/seller', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(editedSeller),
      })
      .catch(() => null);
    if (response?.status === 400) return 'invalid';
    if (!response?.ok) return 'failed';
    seller.value = await response.json();
    return 'saved';
  }

  return { seller, loadError, fetchSeller, updateSeller };
});
