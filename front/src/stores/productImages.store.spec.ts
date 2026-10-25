import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { MAX_IMAGE_BYTES, useProductImagesStore } from './productImages.store';

vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  productImagesApi: { getProductsWithImage: vi.fn(() => Promise.resolve({ data: ['lamp'] })) },
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const imageOf = (size: number) => new File([new Uint8Array(size)], 'lamp.png', { type: 'image/png' });

describe('productImages.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('knows the products that have an image', async () => {
    // Arrange
    const store = useProductImagesStore();
    // Act
    await store.fetchProductsWithImage();
    // Assert
    expect([store.hasImage('lamp'), store.hasImage('mug')]).toEqual([true, false]);
  });

  it('refuses an image of more than 2 MB without sending it', async () => {
    // Act
    const upload = await useProductImagesStore().uploadImage('lamp', imageOf(MAX_IMAGE_BYTES + 1));
    // Assert
    expect(upload).toBe('tooLarge');
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it.each([
    { status: 204, expected: 'saved' },
    { status: 413, expected: 'tooLarge' },
    { status: 415, expected: 'unsupported' },
    { status: 500, expected: 'error' },
  ])('an image answered $status is $expected', async ({ status, expected }) => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: status < 300, status });
    // Act
    const upload = await useProductImagesStore().uploadImage('mug', imageOf(1000));
    // Assert
    expect(upload).toBe(expected);
  });

  it('shows a new image once given', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
    const store = useProductImagesStore();
    // Act
    await store.uploadImage('mug', imageOf(1000));
    // Assert
    expect(store.hasImage('mug')).toBe(true);
    expect(store.imageUrl('mug', 'large')).toContain('v=1');
  });
});
