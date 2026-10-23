import type { CartItems, Item } from '@/models/cart';
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useCartStore = defineStore('cart', () => {
    const cartItems = ref<CartItems | null>(null);
    const apiUrl = import.meta.env.VITE_API_URL;

    async function fetchCartItems() {
        const response = await fetch(`${apiUrl}/api/cart/cart-items`);
        if (response.ok) {
            cartItems.value = await response.json();
        } else {
            console.error('Failed to load cart items');
        }
    }

    // Returns whether the item was saved
    async function saveItem(item: Item): Promise<boolean> {
        console.info('Saving...', item);
        try {
            const response = await fetch(`${apiUrl}/api/cart/save`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(item),
            });
            if (!response.ok) {
                console.error('Failed to save cart item');
                return false;
            }
            const data = await response.json();
            console.info('Item saved : ', data);
            // Reload the cart so that its items, totals and the header badge stay in sync with the server
            await fetchCartItems();
            return true;
        } catch (error) {
            console.error('Failed to save cart item', error);
            return false;
        }
    }

    async function removeItem(item: Item) {
        const itemId = item.itemId;
        if (cartItems.value) {
            cartItems.value.items = cartItems.value.items.filter(item => item.itemId !== itemId);
        }
        await saveItem(item);
    }

    return { cartItems, fetchCartItems, saveItem, removeItem }
});
