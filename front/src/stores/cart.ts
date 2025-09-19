import {defineStore} from 'pinia';
import {ref} from 'vue';

export interface Item {
    itemId: string;
    productId: string;
    productName: string;
    productDescription: string;
    productCategory: string;
    productPrice: number;
    productCurrency: string;
    productQuantity: number;
    itemPrice: number;
}

export const useCartStore = defineStore('cart', () => {
    const items = ref<Item[]>([]);
    const apiUrl = import.meta.env.VITE_API_URL;

    async function fetchItems() {
        const response = await fetch(`${apiUrl}/api/cart/items`);
        if (response.ok) {
            const data = await response.json();
            items.value = Array.isArray(data) ? data : [];
        } else {
            console.error('Failed to load cart items');
        }
    }

    async function saveItem(item: Item) {
        console.info('Saving...', item);
        const response = await fetch(`${apiUrl}/api/cart/save`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(item),
        });
        if (response.ok) {
            const data = await response.json();
            console.info('Item saved : ', data);
        } else {
            console.error('Failed to save cart item');
        }
    }

    async function removeItem(item: Item) {
        const itemId = item.itemId;
        items.value = items.value.filter(item => item.itemId !== itemId);
        await saveItem(item);
    }

    return { items, fetchItems, saveItem, removeItem }
});
