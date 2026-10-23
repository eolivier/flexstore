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

describe('getProduct', () => {
  beforeEach(() => {
    vi.mocked(productsApi.getProduct).mockReset();
  });

  it('returns the product', async () => {
    vi.mocked(productsApi.getProduct).mockResolvedValue({
      data: { id: '3', name: 'Slim Jeans', description: 'Modern fit', category: 'CLOTHING', price: 49, currency: '€' },
    } as never);

    expect(await getProduct('3')).toEqual({
      id: '3', name: 'Slim Jeans', description: 'Modern fit', category: 'CLOTHING', price: 49, currency: '€',
    });
  });

  it('returns null when the product does not exist', async () => {
    vi.mocked(productsApi.getProduct).mockRejectedValue(httpError(404));

    expect(await getProduct('unknown')).toBeNull();
  });

  it('rethrows other errors', async () => {
    vi.mocked(productsApi.getProduct).mockRejectedValue(httpError(500));

    await expect(getProduct('3')).rejects.toThrow();
  });
});
