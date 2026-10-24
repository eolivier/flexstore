import type { CartItems, Item } from '@/models/cart';
import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth.store';

// Id of the cart of a visitor, created by the server on the first item
const CART_ID_KEY = 'flexstore.cartId';
const CART_ID_HEADER = 'X-Cart-Id';

function readCartId(): string | null {
    try {
        return localStorage.getItem(CART_ID_KEY);
    } catch {
        return null;
    }
}

function writeCartId(cartId: string | null) {
    try {
        if (cartId) {
            localStorage.setItem(CART_ID_KEY, cartId);
        } else {
            localStorage.removeItem(CART_ID_KEY);
        }
    } catch {
        // Storage unavailable (private mode...): the visitor cart lasts until the page is reloaded
    }
}

// The cart of the signed-in customer (found with the token), otherwise the cart of the visitor (X-Cart-Id)
export const useCartStore = defineStore('cart', () => {
    const cartItems = ref<CartItems | null>(null);
    // Set when the items saved in the account were added to the cart at sign-in
    const accountItemsMerged = ref(false);
    const apiUrl = import.meta.env.VITE_API_URL;
    const authStore = useAuthStore();

    function cartFetch(path: string, init: RequestInit = {}): Promise<Response> {
        if (authStore.isAuthenticated) {
            return authStore.authFetch(path, init);
        }
        const headers = new Headers(init.headers);
        const cartId = readCartId();
        if (cartId) {
            headers.set(CART_ID_HEADER, cartId);
        }
        return fetch(`${apiUrl}${path}`, { ...init, headers });
    }

    async function fetchCartItems() {
        const response = await cartFetch('/api/cart/cart-items');
        if (response.ok) {
            cartItems.value = await response.json();
        } else {
            console.error('Failed to load cart items');
        }
    }

    // Returns whether the item was saved
    async function saveItem(item: Item): Promise<boolean> {
        try {
            const response = await cartFetch('/api/cart/save', {
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
            const data: { cartId?: string | null } = await response.json();
            if (data.cartId) {
                writeCartId(data.cartId);
            }
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

    // At sign-in, the cart filled as a visitor is merged into the cart of the customer
    async function mergeVisitorCart() {
        const cartId = readCartId();
        if (!cartId) return;
        const visitorLines = cartItems.value?.items.length ?? 0;
        try {
            const response = await authStore.authFetch('/api/cart/merge', {
                method: 'POST',
                headers: { [CART_ID_HEADER]: cartId },
            });
            if (!response.ok) {
                console.error('Failed to merge the visitor cart');
                return;
            }
            writeCartId(null);
            await fetchCartItems();
            accountItemsMerged.value = visitorLines > 0 && (cartItems.value?.items.length ?? 0) > visitorLines;
        } catch (error) {
            console.error('Failed to merge the visitor cart', error);
        }
    }

    watch(() => authStore.isAuthenticated, async (authenticated) => {
        if (authenticated) {
            await mergeVisitorCart();
        }
        // Signed in: the customer cart; signed out: a new visitor cart
        await fetchCartItems();
    });

    return { cartItems, accountItemsMerged, fetchCartItems, saveItem, removeItem }
});
