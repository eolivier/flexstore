import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import type { SocialNetworkUrls } from '@/utils/socialNetworks';

// The seller written on the invoices: the address on one line, the SIRET (14 digits), the intra-community VAT
// number of the shop, how the customers reach it, and how it is incorporated: its legal form, its share capital
// in euros and the city of its trade register (empty in the versions written before they were asked), and its
// accounts on the social networks (empty where it has none)
export interface Seller {
  name: string;
  address: string;
  siret: string;
  vatNumber: string;
  phone: string;
  email: string;
  website: string;
  legalForm: string;
  shareCapital: string;
  registryCity: string;
  socialNetworks: SocialNetworkUrls;
}

// saved; invalid: refused by the shop (a malformed SIRET, VAT number, phone, email, website or share capital);
// failed: not reached
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

  const updateSeller = (editedSeller: Seller) => saveSeller('/api/admin/seller', editedSeller);

  // Finishes the setup assistant: the seller is saved, then the seller setup of the settings is turned on
  const completeSetup = (givenSeller: Seller) => saveSeller('/api/admin/seller/setup', givenSeller);

  async function saveSeller(path: string, editedSeller: Seller): Promise<SellerUpdate> {
    const response = await authStore
      .authFetch(path, {
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

  return { seller, loadError, fetchSeller, updateSeller, completeSetup };
});
