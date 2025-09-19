import {defineStore} from 'pinia';
import {ref} from 'vue';

export interface CartItems {
    items: Item[];
    totalItemsPrice: number;
    itemsCurrency: string;
}

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
        if (cartItems.value) {
            cartItems.value.items = cartItems.value.items.filter(item => item.itemId !== itemId);
        }
        await saveItem(item);
    }

    return { cartItems, fetchCartItems, saveItem, removeItem }
});
