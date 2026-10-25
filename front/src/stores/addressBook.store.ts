import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { PostalAddress } from '@/models/account';
import type { JsonPostalAddress } from '@/generated';
import { useAuthStore } from './auth.store';

// incomplete: the address misses its recipient, street, city or country
export type AddressEdition = 'saved' | 'incomplete' | 'error';

// The forms edit the missing values as empty strings
function editable(address: JsonPostalAddress): PostalAddress {
  return {
    ...address,
    firstName: address.firstName ?? '',
    lastName: address.lastName ?? '',
    companyName: address.companyName ?? '',
    vatNumber: address.vatNumber ?? '',
    complement: address.complement ?? '',
  };
}

// The postal addresses of the signed-in user, in their order: the position of an address designates it
export const useAddressBookStore = defineStore('addressBook', () => {
  const addresses = ref<PostalAddress[]>([]);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function keep(response: Response) {
    const saved: JsonPostalAddress[] = await response.json();
    addresses.value = saved.map(editable);
  }

  async function fetchAddresses() {
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch('/api/accounts/me/addresses').catch(() => null);
    if (response?.ok) {
      await keep(response);
    } else {
      loadError.value = true;
    }
    loading.value = false;
  }

  async function changeAddress(position: number, address: PostalAddress): Promise<AddressEdition> {
    const response = await authStore
      .authFetch(`/api/accounts/me/addresses/${position}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(address),
      })
      .catch(() => null);
    if (response?.status === 400) return 'incomplete';
    if (!response?.ok) return 'error';
    await keep(response);
    return 'saved';
  }

  // Returns whether the address was removed
  async function removeAddress(position: number): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/accounts/me/addresses/${position}`, { method: 'DELETE' })
      .catch(() => null);
    if (!response?.ok) return false;
    await keep(response);
    return true;
  }

  return { addresses, loading, loadError, fetchAddresses, changeAddress, removeAddress };
});
