import { defineStore } from 'pinia';
import type { JsonAccount } from '@/generated';
import { useAuthStore } from './auth.store';

// The account of the signed-in customer, with their phone number and postal addresses
export const useAccountStore = defineStore('account', () => {

  // Returns null when the account cannot be read
  async function fetchAccount(): Promise<JsonAccount | null> {
    try {
      const response = await useAuthStore().authFetch('/api/accounts/me');
      return response.ok ? response.json() : null;
    } catch (error) {
      console.error('Failed to read the account', error);
      return null;
    }
  }

  return { fetchAccount };
});
