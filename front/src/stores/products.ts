import { defineStore } from 'pinia';
import { ref } from 'vue';
import { productsApi } from '@/api.ts';
import type { JsonProduct } from '../generated';

export interface Product {
  id: string;
  name: string;
  description: string;
  category: string;
  price: number;
  currency: string;
}

export const useProductsStore = defineStore('products', () => {
  const products = ref<Product[]>([]);
  const apiUrl = import.meta.env.VITE_API_URL;

  const jsonProducts = ref<JsonProduct[]>([]);
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
    const response = await fetch(`${apiUrl}/api/products/`);
    if (response.ok) {
      products.value = await response.json();
    } else {
      console.error('Error when loading products');
    }
  }

  async function fetchJsonProducts() {
    loading.value = true;
    error.value = null;
    try {
      const res = await productsApi.getProducts();
      jsonProducts.value = res.data;
    } catch (e: any) {
      console.error('Error when loading products', e);
      error.value =
        e?.response?.data?.title || e?.message || 'Unknown error while loading products';
    } finally {
      loading.value = false;
    }
  }

  return { products, jsonProducts, fetchProducts, fetchJsonProducts, addProduct };
});
