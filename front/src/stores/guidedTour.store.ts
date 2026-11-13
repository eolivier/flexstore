import { defineStore } from 'pinia';
import { computed, ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { type AccountGuidedTour, guidedTourOfAccount } from '@/utils/guidedTour';
import { jsonRequest } from '@/utils/jsonRequest';

// The guided tour of the shop, shown to a signed-in customer until they hide it ("Do not show again") or
// until they skip it (it comes back at their next sign-in then). Hiding it is a preference of their account,
// which they change again in their preferences
export const useGuidedTourStore = defineStore('guidedTour', () => {
  const authStore = useAuthStore();
  const accountGuidedTour = ref<AccountGuidedTour>('none');
  const isOpen = ref(false);
  const isShown = computed(() => accountGuidedTour.value !== 'hidden');

  async function readFromAccount(): Promise<AccountGuidedTour> {
    const response = await authStore.authFetch('/api/guided-tour').catch(() => null);
    if (!response?.ok) return 'hidden';
    const preference = await response.json();
    return guidedTourOfAccount[preference.guidedTour] ?? 'hidden';
  }

  async function recordInAccount(guidedTour: 'shown' | 'hidden'): Promise<void> {
    const response = await authStore
      .authFetch('/api/guided-tour', jsonRequest('PUT', { guidedTour: guidedTour.toUpperCase() }))
      .catch(() => null);
    if (response?.ok) accountGuidedTour.value = guidedTour;
  }

  // An account that cannot be read is treated as "hidden": the tour never bothers a customer by mistake
  async function openAtSignIn() {
    accountGuidedTour.value = await readFromAccount();
    isOpen.value = isShown.value;
  }

  watch(
    () => authStore.isAuthenticated,
    (isAuthenticated) => {
      if (isAuthenticated) return openAtSignIn();
      accountGuidedTour.value = 'none';
      isOpen.value = false;
    },
    { immediate: true }
  );

  // The customer ignores the tour: it is not recorded, so it comes back at their next sign-in
  function skip() {
    isOpen.value = false;
  }

  // The customer reached the end of the tour, and may ask not to see it again
  async function finish(doNotShowAgain: boolean) {
    isOpen.value = false;
    if (doNotShowAgain) await recordInAccount('hidden');
  }

  // The customer turned the tour on or off in their preferences: turning it on starts it right away
  async function choose(shown: boolean) {
    await recordInAccount(shown ? 'shown' : 'hidden');
    isOpen.value = shown && isShown.value;
  }

  return { isOpen, isShown, skip, finish, choose };
});
