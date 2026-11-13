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

  it('should fetch the points of the customer', async () => {
    // Arrange
    mockFetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: () =>
        Promise.resolve({
          points: 89,
          purchases: [{ orderNumber: 'AAAA1111', placedAt: '2026-10-24', points: 89 }],
        }),
    });
    const store = useLoyaltyStore();
    // Act
    await store.fetchPoints();
    // Assert
    expect(store.points).toBe(89);
    expect(store.purchases).toHaveLength(1);
    expect(store.loadError).toBe(false);
  });

  it('should report an error when the points could not be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    const store = useLoyaltyStore();
    // Act
    await store.fetchPoints();
    // Assert
    expect(store.loadError).toBe(true);
  });
});
