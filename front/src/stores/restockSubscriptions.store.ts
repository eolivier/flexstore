import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth.store';

// The products the signed-in customer waits for: they are notified by email as soon as one is back in stock
export const useRestockSubscriptionsStore = defineStore('restockSubscriptions', () => {
  const subscribedProductIds = ref<string[]>([]);
  const authStore = useAuthStore();

  function isSubscribed(productId: string | undefined): boolean {
    return productId !== undefined && subscribedProductIds.value.includes(productId);
  }

  async function fetchSubscriptions() {
    const response = await authStore.authFetch('/api/restock-subscriptions').catch(() => null);
    if (response?.ok) {
      const loadedProductIds: string[] = await response.json();
      // A subscription made while the list was loading is kept
      subscribedProductIds.value = [...new Set([...loadedProductIds, ...subscribedProductIds.value])];
    }
  }

  // Returns whether the customer will be notified when the product is back in stock
  async function subscribe(productId: string): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/restock-subscriptions/${encodeURIComponent(productId)}`, { method: 'PUT' })
      .catch(() => null);
    if (!response?.ok) return false;
    if (!isSubscribed(productId)) subscribedProductIds.value = [...subscribedProductIds.value, productId];
    return true;
  }

  // Signed out: the subscriptions are forgotten; signed in: they are loaded
  watch(
    () => authStore.isAuthenticated,
    (authenticated) => {
      if (authenticated) {
        fetchSubscriptions();
      } else {
        subscribedProductIds.value = [];
      }
    },
    { immediate: true }
  );

  return { subscribedProductIds, isSubscribed, fetchSubscriptions, subscribe };
});
