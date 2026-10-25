import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Product } from '@/models/product';
import { getAllProducts, getProduct } from '@/services/product.service';
import { useAuthStore } from './auth.store';

export const useProductsStore = defineStore('products', () => {
  const products = ref<Product[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Returns whether the product was created
  async function addProduct(product: Product): Promise<boolean> {
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

  // Returns null when the product cannot be read
  async function fetchProduct(id: string): Promise<Product | null> {
    try {
      return await getProduct(id);
    } catch (e) {
      console.error('Error when reading product', e);
      return null;
    }
  }

  // Returns whether the product was edited
  async function updateProduct(id: string, product: Product): Promise<boolean> {
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
      products.value = await getAllProducts();
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error while loading products';
    } finally {
      loading.value = false;
    }
  }

  return { products, loading, error, fetchProducts, fetchProduct, addProduct, updateProduct };
});
