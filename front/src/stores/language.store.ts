import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { isShopLanguage, type ShopLanguage } from '@/utils/language';
import { jsonRequest } from '@/utils/jsonRequest';

// NONE: the account holds no language yet
export type AccountLanguage = ShopLanguage | 'none';

// The preferred language of a signed-in customer, kept in their account so that every browser they sign in from
// shows the shop in it: the language of the account applies at sign-in. A visitor has no stored language: the
// address of the page (or their last choice in the menu) decides
export const useLanguageStore = defineStore('language', () => {
  const authStore = useAuthStore();
  // The language the account holds once the customer is signed in, to be applied by the page
  const accountLanguage = ref<AccountLanguage>('none');

  async function readFromAccount(): Promise<AccountLanguage> {
    const response = await authStore.authFetch('/api/language').catch(() => null);
    if (!response?.ok) return 'none';
    const preference = await response.json();
    const language = String(preference.language).toLowerCase();
    return isShopLanguage(language) ? language : 'none';
  }

  watch(
    () => authStore.isAuthenticated,
    async (isAuthenticated) => {
      accountLanguage.value = isAuthenticated ? await readFromAccount() : 'none';
    },
    { immediate: true }
  );

  // The customer chose a language: their account keeps it when they are signed in
  async function choose(language: ShopLanguage): Promise<void> {
    if (!authStore.isAuthenticated) return;
    await authStore
      .authFetch('/api/language', jsonRequest('PUT', { language: language.toUpperCase() }))
      .catch(() => null);
  }

  return { accountLanguage, choose };
});
