import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonEnvironmentVariable } from '@/generated';
import { useAuthStore } from '@/stores/auth.store';

// The environment variables of the back end, for the administrators: the secrets come without their value
export const useEnvironmentStore = defineStore('environment', () => {
  const variables = ref<JsonEnvironmentVariable[]>([]);
  const loaded = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  async function fetchVariables() {
    loadError.value = false;
    const response = await authStore.authFetch('/api/admin/environment').catch(() => null);
    if (response?.ok) {
      variables.value = await response.json();
      loaded.value = true;
    } else {
      loadError.value = true;
    }
  }

  return { variables, loaded, loadError, fetchVariables };
});
