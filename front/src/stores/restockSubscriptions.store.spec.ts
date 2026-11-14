import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useRestockSubscriptionsStore } from './restockSubscriptions.store';
import { useAuthStore } from './auth.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function signIn() {
  const authStore = useAuthStore();
  authStore.token = 'token';
  authStore.currentUser = { id: 'user-1', role: 'USER' } as typeof authStore.currentUser;
}

describe('restockSubscriptions.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
  });

  it('should load the products the customer waits for', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(['p-1', 'p-2']) });
    const store = useRestockSubscriptionsStore();
    // Act
    await store.fetchSubscriptions();
    // Assert
    expect(store.isSubscribed('p-1')).toBe(true);
    expect(store.isSubscribed('unknown')).toBe(false);
  });

  it('should subscribe to the return of a product', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
    const store = useRestockSubscriptionsStore();
    // Act
    const subscribed = await store.subscribe('p-1');
    // Assert
    expect(subscribed).toBe(true);
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringMatching(/\/api\/restock-subscriptions\/p-1$/),
      expect.objectContaining({ method: 'PUT' })
    );
    expect(store.isSubscribed('p-1')).toBe(true);
  });

  it('should not subscribe twice to the same product', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
    const store = useRestockSubscriptionsStore();
    await store.subscribe('p-1');
    // Act
    await store.subscribe('p-1');
    // Assert
    expect(store.subscribedProductIds).toEqual(['p-1']);
  });

  it('should report a subscription that the shop refused', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 409 });
    const store = useRestockSubscriptionsStore();
    // Act
    const subscribed = await store.subscribe('p-1');
    // Assert
    expect(subscribed).toBe(false);
    expect(store.isSubscribed('p-1')).toBe(false);
  });

  it('should forget the subscriptions when the customer signs out', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 204, json: () => Promise.resolve([]) });
    signIn();
    const store = useRestockSubscriptionsStore();
    await store.subscribe('p-1');
    const authStore = useAuthStore();
    // Act
    authStore.token = null;
    authStore.currentUser = null;
    // Assert
    await vi.waitFor(() => expect(store.isSubscribed('p-1')).toBe(false));
  });
});
