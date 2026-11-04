import { defineStore } from 'pinia';
import { computed, ref, watch } from 'vue';
import type { Product } from '@/models/product';
import type { JsonProduct } from '@/generated';
import { fromApi } from '@/mappers/product.mapper';
import { useAuthStore } from '@/stores/auth.store';

// The products the signed-in customer saved to their wishlist, to buy them back later
export const useWishlistStore = defineStore('wishlist', () => {
  const products = ref<Product[]>([]);
  const loading = ref(false);
  const loadError = ref(false);
  const authStore = useAuthStore();

  const savedProductIds = computed(() => new Set(products.value.map((product) => product.id)));

  function isSaved(productId: string | undefined): boolean {
    return productId !== undefined && savedProductIds.value.has(productId);
  }

  async function fetchWishlist() {
    loading.value = true;
    loadError.value = false;
    const response = await authStore.authFetch('/api/wishlist').catch(() => null);
    if (response?.ok) {
      const jsonProducts: JsonProduct[] = await response.json();
      products.value = jsonProducts.map(fromApi);
    } else {
      loadError.value = true;
    }
    loading.value = false;
  }

  // Returns whether the product is saved to the wishlist
  async function save(product: Product): Promise<boolean> {
    if (!product.id) return false;
    const response = await authStore
      .authFetch(`/api/wishlist/${encodeURIComponent(product.id)}`, { method: 'PUT' })
      .catch(() => null);
    if (!response?.ok) return false;
    if (!isSaved(product.id)) products.value = [product, ...products.value];
    return true;
  }

  // Returns whether the product was removed
  async function remove(productId: string): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/wishlist/${encodeURIComponent(productId)}`, { method: 'DELETE' })
      .catch(() => null);
    if (!response?.ok) return false;
    products.value = products.value.filter((product) => product.id !== productId);
    return true;
  }

  // Saves the product when it is not in the wishlist yet, removes it otherwise; returns whether it succeeded
  function toggle(product: Product): Promise<boolean> {
    const productId = product.id;
    if (!productId) return Promise.resolve(false);
    return isSaved(productId) ? remove(productId) : save(product);
  }

  // Signed out: the wishlist is forgotten; signed in: it is loaded
  watch(
    () => authStore.isAuthenticated,
    (authenticated) => {
      if (authenticated) {
        fetchWishlist();
      } else {
        products.value = [];
      }
    }
  );

  return { products, loading, loadError, isSaved, fetchWishlist, save, remove, toggle };
});
