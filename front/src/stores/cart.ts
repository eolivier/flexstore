import { defineStore } from 'pinia';
import { ref } from 'vue';

interface Item {
  itemId: string;
  productName: string;
  productQuantity: number;
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<Item[]>([]);

  async function fetchItems() {
    const response = await fetch('/api/cart/items');
    if (response.ok) {
      items.value = await response.json();
    } else {
      console.error('Failed to load cart items');
    }
  }
  return { items, fetchItems };
});
