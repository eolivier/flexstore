import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

// The requests to the API in progress, so that the page can show that something is loading
export const useLoadingStore = defineStore('loading', () => {
  const pendingRequests = ref(0);
  const isLoading = computed(() => pendingRequests.value > 0);

  async function track<T>(request: Promise<T>): Promise<T> {
    pendingRequests.value++;
    try {
      return await request;
    } finally {
      pendingRequests.value--;
    }
  }

  return { isLoading, track };
});
