import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { forgetColorMode } from '@/utils/colorMode';
import { type CookieChoice, rememberCookieChoice, storedCookieChoice } from '@/utils/cookieConsent';

// The cookie choice of the customer: asked by the banner at the first visit, changed in their account
export const useCookieConsentStore = defineStore('cookieConsent', () => {
  const choice = ref<CookieChoice>(storedCookieChoice());
  const isUndecided = computed(() => choice.value === 'undecided');
  const preferencesAccepted = computed(() => choice.value === 'accepted');

  function choose(newChoice: CookieChoice) {
    choice.value = newChoice;
    rememberCookieChoice(newChoice);
    if (newChoice !== 'accepted') {
      forgetColorMode();
    }
  }

  return { isUndecided, preferencesAccepted, choose };
});
