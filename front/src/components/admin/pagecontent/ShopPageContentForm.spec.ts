import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ShopPageContentForm from './ShopPageContentForm.vue';
import { useShopPageContentStore, type ShopPageContentType } from '@/stores/shopPageContent.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function mountForm(type: ShopPageContentType, text: string) {
  useShopPageContentStore().texts[type] = text;
  return mount(ShopPageContentForm, { props: { type }, global: { plugins: [vuetify, i18n] } });
}

describe('ShopPageContentForm.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('shows the current text of the content', () => {
    // Act
    const wrapper = mountForm('META_KEY_WORDS', 'shop, demo');
    // Assert
    expect((wrapper.find('textarea').element as HTMLTextAreaElement).value).toBe('shop, demo');
  });

  it('saves the edited text', async () => {
    // Arrange
    const edited = { contents: [{ type: 'META_KEY_WORDS', text: 'shop, new' }] };
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(edited) });
    const wrapper = mountForm('META_KEY_WORDS', 'shop, demo');
    await wrapper.find('textarea').setValue('shop, new');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/shop-page-contents/meta-key-words');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ text: 'shop, new' });
    expect(wrapper.text()).toContain('Saved: the visitors now read this content.');
  });

  it('clears a content with an empty text', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve({ contents: [] }) });
    const wrapper = mountForm('META_SHOP_DESCRIPTION', 'Demo shop');
    await wrapper.find('textarea').setValue('');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ text: '' });
  });

  it.each(['G-123 456', 'G-1;alert(1)'])('does not save the identifier %s', async (identifier) => {
    // Arrange
    const wrapper = mountForm('GOOGLE_ANALYTICS_ID', '');
    await wrapper.find('textarea').setValue(identifier);
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(mockFetch).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain('Letters, digits and hyphens only');
  });

  it('tells when the shop refuses the content', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: false, status: 400 });
    const wrapper = mountForm('META_KEY_WORDS', 'shop');
    await wrapper.find('textarea').setValue('shop, demo');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('The shop refused the content');
  });
});
