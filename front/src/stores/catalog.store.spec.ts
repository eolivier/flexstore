import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import type { Product } from '@/models/product';
import { getCatalogPage } from '@/services/product.service';
import { useCatalogStore } from './catalog.store';

vi.mock('@/services/product.service', () => ({ getCatalogPage: vi.fn() }));

const deskLamp: Product = {
  id: 'p-1',
  sku: 'LAMP-1',
  name: 'Desk lamp',
  description: '',
  category: 'HOME',
  currency: 'EUR',
};
const query = { search: 'lamp', category: '', sort: 'name' as const, page: 0 };

describe('catalog store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.mocked(getCatalogPage).mockReset();
  });

  it('should keep the products of the page and the total of the products that match', async () => {
    // Arrange
    vi.mocked(getCatalogPage).mockResolvedValue({ products: [deskLamp], total: 12 });
    const store = useCatalogStore();
    // Act
    await store.fetchCatalogPage(query);
    // Assert
    expect(store.products).toEqual([deskLamp]);
    expect(store.total).toBe(12);
    expect(store.error).toBeNull();
  });

  it('should ask the service for the query in the language of the application', async () => {
    // Arrange
    vi.mocked(getCatalogPage).mockResolvedValue({ products: [], total: 0 });
    const store = useCatalogStore();
    // Act
    await store.fetchCatalogPage(query);
    // Assert
    expect(getCatalogPage).toHaveBeenCalledWith(expect.any(String), query);
  });

  it('should report a catalog that cannot be read', async () => {
    // Arrange
    vi.mocked(getCatalogPage).mockRejectedValue(new Error('Network down'));
    const store = useCatalogStore();
    // Act
    await store.fetchCatalogPage(query);
    // Assert
    expect(store.error).toBe('Network down');
    expect(store.loading).toBe(false);
  });
});
