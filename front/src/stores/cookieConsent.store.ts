import { defineStore } from 'pinia';
import { computed, ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { forgetColorMode } from '@/utils/colorMode';
import {
  applyCookieChoice,
  clearStoredCookieChoice,
  type CookieChoice,
  rememberCookieChoice,
  storedCookieChoice,
} from '@/utils/cookieConsent';

const choiceOfAccount: Record<string, CookieChoice> = { ACCEPTED: 'accepted', REFUSED: 'refused' };
const accountOfChoice: Record<string, string> = { accepted: 'ACCEPTED', refused: 'REFUSED' };

// The cookie choice of the customer, asked by the banner. A visitor keeps it in the browser; once signed in, the
// customer has it in their account (valid 6 months): the choice of the browser is reported there and removed
export const useCookieConsentStore = defineStore('cookieConsent', () => {
  const authStore = useAuthStore();
  const choice = ref<CookieChoice>(storedCookieChoice());
  const isSynchronizing = ref(false);
  const isUndecided = computed(() => choice.value === 'undecided' && !isSynchronizing.value);
  const preferencesAccepted = computed(() => choice.value === 'accepted');

  function applyChoice(newChoice: CookieChoice) {
    choice.value = newChoice;
    applyCookieChoice(newChoice);
    if (newChoice !== 'accepted') {
      forgetColorMode();
    }
  }

  // Returns whether the account recorded the choice
  async function recordInAccount(newChoice: CookieChoice): Promise<boolean> {
    const response = await authStore
      .authFetch('/api/cookie-consent', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ choice: accountOfChoice[newChoice] }),
      })
      .catch(() => null);
    return response?.ok === true;
  }

  async function readFromAccount(): Promise<CookieChoice> {
    const response = await authStore.authFetch('/api/cookie-consent').catch(() => null);
    if (!response?.ok) return storedCookieChoice();
    const consent = await response.json();
    return choiceOfAccount[consent.choice] ?? 'undecided';
  }

  // At sign-in: the choice made in the browser goes to the account and leaves the browser; without one, the
  // account tells (undecided once its choice is 6 months old)
  async function synchronizeWithAccount() {
    isSynchronizing.value = true;
    try {
      const browserChoice = storedCookieChoice();
      if (browserChoice === 'undecided') {
        applyChoice(await readFromAccount());
      } else if (await recordInAccount(browserChoice)) {
        clearStoredCookieChoice();
        applyChoice(browserChoice);
      }
    } finally {
      isSynchronizing.value = false;
    }
  }

  // Signed out, the choice of the browser applies again (none once it was reported to the account)
  function followBrowser() {
    applyChoice(storedCookieChoice());
  }

  watch(
    () => authStore.isAuthenticated,
    (isAuthenticated) => (isAuthenticated ? synchronizeWithAccount() : followBrowser()),
    { immediate: true }
  );

  async function choose(newChoice: CookieChoice) {
    applyChoice(newChoice);
    if (!authStore.isAuthenticated) {
      rememberCookieChoice(newChoice);
    } else if (!(await recordInAccount(newChoice))) {
      // The account could not be reached: the browser keeps the choice, reported at the next sign-in
      rememberCookieChoice(newChoice);
    }
  }

  return { isUndecided, preferencesAccepted, choose };
});
