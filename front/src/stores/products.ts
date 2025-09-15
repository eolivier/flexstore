import { defineStore } from 'pinia';
import { ref } from 'vue';

interface Product {
    id: string;
    name: string;
    description: string;
    price: number;
    currency: string;
}

export const useProductsStore = defineStore('products', () => {
    const products = ref<Product[]>([]);
    const apiUrl = import.meta.env.VITE_API_URL;

    async function fetchProducts() {
        const response = await fetch(`${apiUrl}/api/products/`);
        if (response.ok) {
            products.value = await response.json();
        } else {
            console.error('Error when loading products');
        }
    }

    return {products, fetchProducts};
});
