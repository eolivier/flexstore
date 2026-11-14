import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { type ColorMode, hasRememberedColorMode, initialColorMode, rememberColorMode } from '@/utils/colorMode';
import { jsonRequest } from '@/utils/jsonRequest';

// NONE: the account holds no mode yet
export type AccountColorMode = ColorMode | 'none';

const colorModeOfAccount: Record<string, AccountColorMode> = { LIGHT: 'light', DARK: 'dark', NONE: 'none' };
const accountOfColorMode: Record<ColorMode, string> = { light: 'LIGHT', dark: 'DARK' };

// The display mode of the customer, kept for their next visits. A visitor keeps it in the browser (once they
// accepted the preference cookies); once signed in, the customer has it in their account, so every browser they
// sign in from shows the shop the way they chose: the mode of the account applies at sign-in and, when the
// account has none yet, the one chosen in this browser is reported there
export const useColorModeStore = defineStore('colorMode', () => {
  const authStore = useAuthStore();
  // The mode the account holds once the customer is signed in, to be applied by the page
  const accountColorMode = ref<AccountColorMode>('none');

  async function recordInAccount(colorMode: ColorMode): Promise<boolean> {
    const response = await authStore
      .authFetch('/api/color-mode', jsonRequest('PUT', { colorMode: accountOfColorMode[colorMode] }))
      .catch(() => null);
    return response?.ok === true;
  }

  async function readFromAccount(): Promise<AccountColorMode> {
    const response = await authStore.authFetch('/api/color-mode').catch(() => null);
    if (!response?.ok) return 'none';
    const preference = await response.json();
    return colorModeOfAccount[preference.colorMode] ?? 'none';
  }

  async function synchronizeWithAccount() {
    const storedColorMode = await readFromAccount();
    if (storedColorMode === 'none' && hasRememberedColorMode()) {
      await recordInAccount(initialColorMode());
    }
    accountColorMode.value = storedColorMode;
  }

  watch(
    () => authStore.isAuthenticated,
    (isAuthenticated) => (isAuthenticated ? synchronizeWithAccount() : (accountColorMode.value = 'none')),
    { immediate: true }
  );

  // The customer switched the mode: their account keeps it, or else their browser does
  async function choose(colorMode: ColorMode) {
    if (!authStore.isAuthenticated) {
      rememberColorMode(colorMode);
    } else if (await recordInAccount(colorMode)) {
      // The page applies the mode of the account whenever it is shown: it must be the one just chosen
      accountColorMode.value = colorMode;
    } else {
      rememberColorMode(colorMode);
    }
  }

  return { accountColorMode, choose };
});
