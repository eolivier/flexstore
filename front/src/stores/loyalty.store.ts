import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonLoyaltyPurchase, JsonLoyaltyStatement } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';

// The loyalty points of the signed-in customer: a line for each paid order (1 point for each euro or dollar
// spent) and the total
export const useLoyaltyStore = defineStore('loyalty', () => {
  const points = ref(0);
  const purchases = ref<JsonLoyaltyPurchase[]>([]);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchPoints() {
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch('/api/loyalty').catch(() => null);
    if (response?.ok) {
      const statement: JsonLoyaltyStatement = await response.json();
      points.value = statement.points;
      purchases.value = statement.purchases;
    } else {
      loadError.value = true;
    }
    loading.value = false;
  }

  return { points, purchases, loading, loadError, fetchPoints };
});
