import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonLoyaltyPoints } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';

// The loyalty points of the signed-in customer: 1 point for each euro or dollar spent
export const useLoyaltyStore = defineStore('loyalty', () => {
  const points = ref(0);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchPoints() {
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch('/api/loyalty').catch(() => null);
    if (response?.ok) {
      const loyaltyPoints: JsonLoyaltyPoints = await response.json();
      points.value = loyaltyPoints.points;
    } else {
      loadError.value = true;
    }
    loading.value = false;
  }

  return { points, loading, loadError, fetchPoints };
});
