import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useCartStore } from './cart.store';
import { useAuthStore } from './auth.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const CART_ID = '7c9e6679-7425-40de-944b-e07fc1f90ae7';
const emptyCart = { items: [], totalItemsPrice: 0, itemsCurrency: '€' };
const cartWith = (lines: number) => ({
  items: Array.from({ length: lines }, (_, i) => ({
    itemId: `item-${i}`,
    productId: `${i}`,
    productQuantity: 1,
  })),
  totalItemsPrice: lines * 10,
  itemsCurrency: '€',
});
const item = { itemId: '', productId: '1', productQuantity: 1 };

// Answers each API call with a JSON body chosen by path
function respondWith(bodies: Record<string, unknown>) {
  mockFetch.mockImplementation((url: string) => {
    const path = Object.keys(bodies).find((p) => url.endsWith(p));
    if (url.endsWith('/api/cart/merge')) return Promise.resolve({ ok: true, status: 204 });
    return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve(path ? bodies[path] : {}) });
  });
}

function callTo(path: string) {
  return mockFetch.mock.calls.find(([url]) => url.endsWith(path));
}

function headersOf(path: string) {
  return new Headers(callTo(path)?.[1]?.headers);
}

async function signIn() {
  const authStore = useAuthStore();
  await authStore.login('john@example.com', 'secret');
  await vi.waitFor(() => expect(callTo('/api/cart/cart-items')).toBeDefined());
}

describe('cart.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    localStorage.clear();
  });

  it('should load the cart without cart id when the visitor has none', async () => {
    // Arrange
    respondWith({ '/api/cart/cart-items': emptyCart });
    const store = useCartStore();
    // Act
    await store.fetchCartItems();
    // Assert
    expect(headersOf('/api/cart/cart-items').has('X-Cart-Id')).toBe(false);
    expect(store.cartItems).toEqual(emptyCart);
  });

  it('should keep the cart id returned for a visitor', async () => {
    // Arrange
    respondWith({ '/api/cart/save': { status: 'ok', cartId: CART_ID }, '/api/cart/cart-items': cartWith(1) });
    const store = useCartStore();
    // Act
    await store.saveItem(item);
    // Assert
    expect(localStorage.getItem('flexstore.cartId')).toBe(CART_ID);
  });

  it('should send the cart id of the visitor', async () => {
    // Arrange
    localStorage.setItem('flexstore.cartId', CART_ID);
    respondWith({ '/api/cart/cart-items': cartWith(1) });
    const store = useCartStore();
    // Act
    await store.fetchCartItems();
    // Assert
    expect(headersOf('/api/cart/cart-items').get('X-Cart-Id')).toBe(CART_ID);
  });

  it('should load the cart of the customer with the token at sign-in', async () => {
    // Arrange
    respondWith({
      '/api/users/login': {
        token: 'jwt-token',
        expiresAt: '2030-01-01T00:00:00Z',
        user: { id: 'u-1', name: 'John', email: 'john@example.com', role: 'USER' },
      },
      '/api/cart/cart-items': cartWith(2),
    });
    const store = useCartStore();
    // Act
    await signIn();
    // Assert
    expect(headersOf('/api/cart/cart-items').get('Authorization')).toBe('Bearer jwt-token');
    await vi.waitFor(() => expect(store.cartItems?.items).toHaveLength(2));
  });

  it('should merge the visitor cart at sign-in and forget its id', async () => {
    // Arrange
    localStorage.setItem('flexstore.cartId', CART_ID);
    respondWith({
      '/api/users/login': {
        token: 'jwt-token',
        expiresAt: '2030-01-01T00:00:00Z',
        user: { id: 'u-1', name: 'John', email: 'john@example.com', role: 'USER' },
      },
      '/api/cart/cart-items': cartWith(3),
    });
    const store = useCartStore();
    store.cartItems = cartWith(1);
    // Act
    await signIn();
    // Assert
    const mergeHeaders = headersOf('/api/cart/merge');
    expect(mergeHeaders.get('X-Cart-Id')).toBe(CART_ID);
    expect(mergeHeaders.get('Authorization')).toBe('Bearer jwt-token');
    await vi.waitFor(() => expect(store.accountItemsMerged).toBe(true));
    expect(localStorage.getItem('flexstore.cartId')).toBeNull();
  });
});
