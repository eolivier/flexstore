import { defineStore } from 'pinia';
import { ref } from 'vue';

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

    return {products, fetchProducts, addProduct};
});
