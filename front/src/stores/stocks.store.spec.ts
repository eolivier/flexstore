import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useStocksStore } from './stocks.store';

vi.mock('@/api', () => ({
  stocksApi: { getStocks: vi.fn(() => Promise.resolve({ data: [{ productId: 'p-1', quantity: 4 }] })) },
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('stocks.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it.each([
    { productId: 'p-1', expected: 4 },
    { productId: 'p-2', expected: 0 },
  ])('the stock of $productId is $expected once loaded', async ({ productId, expected }) => {
    // Arrange
    const store = useStocksStore();
    // Act
    await store.fetchStocks();
    // Assert
    expect(store.quantityOf(productId)).toBe(expected);
  });

  it('knows no stock before they are loaded', () => {
    // Act
    const quantity = useStocksStore().quantityOf('p-1');
    // Assert
    expect(quantity).toBeNull();
  });

  it('keeps the stock given by an administrator', async () => {
    // Arrange
    const jsonStock = { productId: 'p-1', quantity: 9 };
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(jsonStock) });
    const store = useStocksStore();
    await store.fetchStocks();
    // Act
    const changed = await store.changeStock('p-1', 9);
    // Assert
    expect(changed).toBe(true);
    expect(store.quantityOf('p-1')).toBe(9);
  });
});
