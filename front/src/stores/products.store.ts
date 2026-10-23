import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Product } from '@/models/product';
import { getAllProducts } from '@/services/product.service';

export const useProductsStore = defineStore('products', () => {
  const apiUrl = import.meta.env.VITE_API_URL;

  const products = ref<Product[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Returns whether the product was created
  async function addProduct(product: Product): Promise<boolean> {
    try {
      const response = await fetch(`${apiUrl}/api/products`, {
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

  async function fetchProducts() {
    loading.value = true;
    error.value = null;
    try {
      products.value = await getAllProducts();
    } catch (e: any) {
      error.value = e?.message ?? 'Unknown error while loading products';
    } finally {
      loading.value = false;
    }
  }

  return { products, loading, error, fetchProducts, addProduct };
});
