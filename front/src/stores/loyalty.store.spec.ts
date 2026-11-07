import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useLoyaltyStore } from './loyalty.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('loyalty.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('fetches the points of the customer', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({ points: 89 }) });
    const store = useLoyaltyStore();
    // Act
    await store.fetchPoints();
    // Assert
    expect(store.points).toBe(89);
    expect(store.loadError).toBe(false);
  });

  it('reports an error when the points could not be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    const store = useLoyaltyStore();
    // Act
    await store.fetchPoints();
    // Assert
    expect(store.loadError).toBe(true);
  });
});
