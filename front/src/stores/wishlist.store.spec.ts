import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useWishlistStore } from './wishlist.store';
import { useAuthStore } from './auth.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const deskLamp = {
  id: 'p-1',
  sku: 'LAMP-DESK',
  name: 'Desk lamp',
  description: 'Warm light',
  category: 'HOME',
  price: 35,
  currency: '€',
};
const officeChair = {
  id: 'p-2',
  sku: 'CHAIR-OFFICE',
  name: 'Office chair',
  description: 'Ergonomic',
  category: 'HOME',
  price: 120,
  currency: '€',
};

function respondToFetch(body: unknown, ok = true) {
  mockFetch.mockImplementation((url: string) => {
    if (url.endsWith('/api/wishlist')) {
      return Promise.resolve({ ok, status: ok ? 200 : 500, json: () => Promise.resolve(body) });
    }
    return Promise.resolve({ ok, status: ok ? 204 : 404 });
  });
}

describe('wishlist.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
  });

  it('loads the products saved by the customer', async () => {
    // Arrange
    respondToFetch([deskLamp, officeChair]);
    const store = useWishlistStore();
    // Act
    await store.fetchWishlist();
    // Assert
    expect(store.products.map((product) => product.id)).toEqual(['p-1', 'p-2']);
    expect(store.isSaved('p-1')).toBe(true);
    expect(store.isSaved('unknown')).toBe(false);
  });

  it('reports an error when the wishlist could not be loaded', async () => {
    // Arrange
    respondToFetch(null, false);
    const store = useWishlistStore();
    // Act
    await store.fetchWishlist();
    // Assert
    expect(store.loadError).toBe(true);
  });

  it('saves a product not saved yet', async () => {
    // Arrange
    respondToFetch([]);
    const store = useWishlistStore();
    // Act
    const saved = await store.save(deskLamp);
    // Assert
    expect(saved).toBe(true);
    expect(store.isSaved('p-1')).toBe(true);
    const [url, init] = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
    expect(url).toMatch(/\/api\/wishlist\/p-1$/);
    expect(init.method).toBe('PUT');
  });

  it('removes a saved product', async () => {
    // Arrange
    respondToFetch([deskLamp]);
    const store = useWishlistStore();
    await store.fetchWishlist();
    // Act
    const removed = await store.remove('p-1');
    // Assert
    expect(removed).toBe(true);
    expect(store.isSaved('p-1')).toBe(false);
    const [url, init] = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
    expect(url).toMatch(/\/api\/wishlist\/p-1$/);
    expect(init.method).toBe('DELETE');
  });

  it('keeps the product saved when the removal fails', async () => {
    // Arrange
    respondToFetch([deskLamp]);
    const store = useWishlistStore();
    await store.fetchWishlist();
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    // Act
    const removed = await store.remove('p-1');
    // Assert
    expect(removed).toBe(false);
    expect(store.isSaved('p-1')).toBe(true);
  });

  it('toggles a product in, then out of the wishlist', async () => {
    // Arrange
    respondToFetch([]);
    const store = useWishlistStore();
    // Act
    await store.toggle(deskLamp);
    const savedAfterFirstToggle = store.isSaved('p-1');
    await store.toggle(deskLamp);
    // Assert
    expect(savedAfterFirstToggle).toBe(true);
    expect(store.isSaved('p-1')).toBe(false);
  });

  it('loads the wishlist of the customer at sign-in, and forgets it at sign-out', async () => {
    // Arrange
    mockFetch.mockImplementation((url: string) => {
      if (url.endsWith('/api/users/login')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () =>
            Promise.resolve({
              token: 'jwt-token',
              expiresAt: '2030-01-01T00:00:00Z',
              user: { id: 'u-1', name: 'Jane', email: 'jane@example.com', role: 'USER' },
            }),
        });
      }
      return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve([deskLamp]) });
    });
    const store = useWishlistStore();
    const authStore = useAuthStore();
    // Act
    await authStore.login('jane@example.com', 'secret');
    await vi.waitFor(() => expect(store.products).toHaveLength(1));
    authStore.logout();
    // Assert
    await vi.waitFor(() => expect(store.products).toHaveLength(0));
  });
});
