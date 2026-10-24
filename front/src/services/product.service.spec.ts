import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';
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

const jeans = { id: '3', name: 'Slim Jeans', description: 'Modern fit', category: 'CLOTHING', price: 49, currency: '€' };

describe('getProduct', () => {
  beforeEach(() => {
    vi.mocked(productsApi.getProduct).mockReset();
  });

  it('returns the product', async () => {
    // Arrange
    vi.mocked(productsApi.getProduct).mockResolvedValue({ data: jeans } as never);
    // Act
    const product = await getProduct('3');
    // Assert
    expect(product).toEqual(jeans);
  });

  it('returns null when the product does not exist', async () => {
    // Arrange
    vi.mocked(productsApi.getProduct).mockRejectedValue(httpError(404));
    // Act
    const product = await getProduct('unknown');
    // Assert
    expect(product).toBeNull();
  });

  it('rethrows other errors', async () => {
    // Arrange
    vi.mocked(productsApi.getProduct).mockRejectedValue(httpError(500));
    // Act
    const result = getProduct('3');
    // Assert
    await expect(result).rejects.toThrow();
  });
});
