import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { productsApi } from '@/api';
import { getProduct } from './product.service';

vi.mock('@/api', () => ({ productsApi: { getProduct: vi.fn() } }));

const httpError = (status: number) =>
  new AxiosError('error', 'ERR', undefined, undefined, {
    status,
    statusText: '',
    data: {},
    headers: {},
    config: { headers: new AxiosHeaders() },
  });

const jeans = {
  id: '3',
  name: 'Slim Jeans',
  sku: 'CLO-3',
  description: 'Modern fit',
  category: 'CLOTHING',
  price: 49,
  currency: '€',
};

describe('getProduct', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.mocked(productsApi.getProduct).mockReset();
  });

  it('should return the product', async () => {
    // Arrange
    vi.mocked(productsApi.getProduct).mockResolvedValue({ data: jeans } as never);
    // Act
    const product = await getProduct('3', 'en');
    // Assert
    expect(product).toEqual(jeans);
  });

  it('should ask the product in the language given only', async () => {
    // Arrange
    vi.mocked(productsApi.getProduct).mockResolvedValue({ data: jeans } as never);
    // Act
    await getProduct('3', 'fr');
    // Assert
    expect(productsApi.getProduct).toHaveBeenCalledWith('3', 'fr');
  });

  it('should return null when the product does not exist', async () => {
    // Arrange
    vi.mocked(productsApi.getProduct).mockRejectedValue(httpError(404));
    // Act
    const product = await getProduct('unknown', 'en');
    // Assert
    expect(product).toBeNull();
  });

  it('should rethrow other errors', async () => {
    // Arrange
    vi.mocked(productsApi.getProduct).mockRejectedValue(httpError(500));
    // Act
    const productLookup = getProduct('3', 'en');
    // Assert
    await expect(productLookup).rejects.toThrow();
  });
});
