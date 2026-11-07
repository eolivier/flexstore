import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useShopPageContentStore } from './shopPageContent.store';

const getShopPageContents = vi.fn();
vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  shopPageContentApi: { getShopPageContents: () => getShopPageContents() },
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const contents = {
  contents: [
    { type: 'META_KEY_WORDS', text: 'shop, demo' },
    { type: 'GOOGLE_ANALYTICS_ID', text: '' },
    { type: 'META_SHOP_DESCRIPTION', text: 'Demo shop' },
  ],
};

describe('shopPageContent.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    getShopPageContents.mockReset();
  });

  it('reads the contents of the shop pages', async () => {
    // Arrange
    getShopPageContents.mockResolvedValue({ data: contents });
    const store = useShopPageContentStore();
    // Act
    await store.fetchContents();
    // Assert
    expect(store.textOf('META_KEY_WORDS')).toBe('shop, demo');
    expect(store.textOf('GOOGLE_ANALYTICS_ID')).toBe('');
    expect(store.loadError).toBe(false);
  });

  it('tells when the contents could not be read', async () => {
    // Arrange
    getShopPageContents.mockRejectedValue(new Error('network'));
    const store = useShopPageContentStore();
    // Act
    await store.fetchContents();
    // Assert
    expect(store.loadError).toBe(true);
  });

  it('reads a content never loaded as an empty text', () => {
    // Act
    const store = useShopPageContentStore();
    // Assert
    expect(store.textOf('META_SHOP_DESCRIPTION')).toBe('');
  });

  it('edits the content of a type and keeps what the shop answers', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(contents) });
    const store = useShopPageContentStore();
    // Act
    const edit = await store.editContent('META_KEY_WORDS', 'shop, demo');
    // Assert
    expect(edit).toBe('saved');
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/shop-page-contents/meta-key-words');
    expect(mockFetch.mock.calls[0][1].method).toBe('PUT');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ text: 'shop, demo' });
    expect(store.textOf('META_KEY_WORDS')).toBe('shop, demo');
  });

  it('tells when the shop refuses a content', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: false, status: 400 });
    const store = useShopPageContentStore();
    // Act
    const edit = await store.editContent('GOOGLE_ANALYTICS_ID', 'G-1;alert(1)');
    // Assert
    expect(edit).toBe('invalid');
  });

  it('tells when the shop could not be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValueOnce(new Error('network'));
    const store = useShopPageContentStore();
    // Act
    const edit = await store.editContent('META_KEY_WORDS', 'shop');
    // Assert
    expect(edit).toBe('failed');
  });
});
