import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import type { JsonLowStock } from '@/generated';
import { useStockAlertsStore } from './stockAlerts.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const deskLampLowStock = { productId: 'p-1' } as JsonLowStock;
const answer = (status: number, body?: unknown) =>
  mockFetch.mockResolvedValueOnce({ ok: status < 300, status, json: () => Promise.resolve(body) });

describe('stockAlerts.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should load the low stocks', async () => {
    // Arrange
    answer(200, [deskLampLowStock]);
    const store = useStockAlertsStore();
    // Act
    await store.fetchLowStocks();
    // Assert
    expect(store.lowStocks).toEqual([deskLampLowStock]);
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/stocks/low');
  });

  it('should keep the low stocks as they were when the API refuses', async () => {
    // Arrange
    answer(200, [deskLampLowStock]);
    const store = useStockAlertsStore();
    await store.fetchLowStocks();
    answer(500);
    // Act
    await store.fetchLowStocks();
    // Assert
    expect(store.lowStocks).toEqual([deskLampLowStock]);
  });

  it('should keep the low stocks as they were when the API cannot be reached', async () => {
    // Arrange
    answer(200, [deskLampLowStock]);
    const store = useStockAlertsStore();
    await store.fetchLowStocks();
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    // Act
    await store.fetchLowStocks();
    // Assert
    expect(store.lowStocks).toEqual([deskLampLowStock]);
  });

  it('should read the threshold of a product', async () => {
    // Arrange
    answer(200, { threshold: 3 });
    const store = useStockAlertsStore();
    // Act
    const threshold = await store.thresholdOf('p/1');
    // Assert
    expect(threshold).toBe(3);
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/stocks/p%2F1/alert');
  });

  it('should read no threshold when the API refuses', async () => {
    // Arrange
    answer(404);
    const store = useStockAlertsStore();
    // Act
    const threshold = await store.thresholdOf('p-1');
    // Assert
    expect(threshold).toBeNull();
  });

  it('should read no threshold when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    const store = useStockAlertsStore();
    // Act
    const threshold = await store.thresholdOf('p-1');
    // Assert
    expect(threshold).toBeNull();
  });

  it('should send the new threshold of a product', async () => {
    // Arrange
    answer(200);
    const store = useStockAlertsStore();
    // Act
    const changed = await store.changeThreshold('p-1', 7);
    // Assert
    expect(changed).toBe(true);
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/stocks/p-1/alert');
    expect(mockFetch.mock.calls[0][1].method).toBe('PUT');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ threshold: 7 });
  });

  it('should not change the threshold when the API refuses', async () => {
    // Arrange
    answer(400);
    const store = useStockAlertsStore();
    // Act
    const changed = await store.changeThreshold('p-1', -1);
    // Assert
    expect(changed).toBe(false);
  });

  it('should not change the threshold when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    const store = useStockAlertsStore();
    // Act
    const changed = await store.changeThreshold('p-1', 7);
    // Assert
    expect(changed).toBe(false);
  });
});
