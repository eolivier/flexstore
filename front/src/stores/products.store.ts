import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import i18n from '@/plugins/i18n';
import type { AdminProduct, Product, ProductInput } from '@/models/product';
import { getAllProducts } from '@/services/product.service';
import { useAuthStore } from './auth.store';

export const useProductsStore = defineStore('products', () => {
  const products = ref<Product[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Returns whether the product was created
  async function addProduct(product: ProductInput): Promise<boolean> {
    try {
      const response = await useAuthStore().authFetch('/api/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
      });
      if (!response.ok) {
        console.error('Error when creating product', await response.text());
        return false;
      }
      await fetchProducts();
      return true;
    } catch (error) {
      console.error('Error when creating product', error);
      return false;
    }
  }

  // The product with its translations in every language, which the administrator edits; null when it cannot
  // be read
  async function fetchAdminProduct(id: string): Promise<AdminProduct | null> {
    try {
      const response = await useAuthStore().authFetch(`/api/admin/products/${encodeURIComponent(id)}`);
      if (!response.ok) return null;
      return await response.json();
    } catch (e) {
      console.error('Error when reading product', e);
      return null;
    }
  }

  // Returns whether the product was edited
  async function updateProduct(id: string, product: ProductInput): Promise<boolean> {
    try {
      const response = await useAuthStore().authFetch(`/api/products/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
      });
      if (!response.ok) {
        console.error('Error when editing product', await response.text());
        return false;
      }
      await fetchProducts();
      return true;
    } catch (e) {
      console.error('Error when editing product', e);
      return false;
    }
  }

  async function fetchProducts() {
    loading.value = true;
    error.value = null;
    try {
      products.value = await getAllProducts(i18n.global.locale.value);
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error while loading products';
    } finally {
      loading.value = false;
    }
  }

  // The products are loaded in the language of the customer only: they are loaded again when it changes
  watch(
    () => i18n.global.locale.value,
    () => {
      if (products.value.length > 0) fetchProducts();
    }
  );

  return { products, loading, error, fetchProducts, fetchAdminProduct, addProduct, updateProduct };
});
