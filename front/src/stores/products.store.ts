import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Product } from '@/models/product';
import { getAllProducts } from '@/services/product.service';

export const useProductsStore = defineStore('products', () => {
  const apiUrl = import.meta.env.VITE_API_URL;

  const products = ref<Product[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function addProduct(product: Product) {
    const response = await fetch(`${apiUrl}/api/products`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(product),
    });
    if (response.ok) {
      const text = await response.text();
      if (text) {
        const data = JSON.parse(text);
        console.log('Product created:', data);
      } else {
        console.log('Product created, but no content returned');
      }
    } else {
      // Gérer l’erreur
      const error = await response.text();
      console.error('Error when creating product', error);
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

  return { products, fetchProducts, addProduct };
});
