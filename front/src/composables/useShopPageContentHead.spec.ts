import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils';
import { createHead } from '@unhead/vue/client';
import { defineComponent, h } from 'vue';
import { createPinia, setActivePinia } from 'pinia';
import { useShopPageContentHead } from './useShopPageContentHead';
import { useShopPageContentStore } from '@/stores/shopPageContent.store';
import { i18n, vuetify } from '@/test/setup';

const getShopPageContents = vi.fn();
vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  shopPageContentApi: { getShopPageContents: () => getShopPageContents() },
}));

const Host = defineComponent({
  setup() {
    useShopPageContentHead();
    return () => h('div');
  },
});

let page: VueWrapper;

function contentsOf(keyWords: string, analyticsId: string) {
  return {
    data: {
      contents: [
        { type: 'META_KEY_WORDS', text: keyWords },
        { type: 'GOOGLE_ANALYTICS_ID', text: analyticsId },
      ],
    },
  };
}

async function openPage(cookieChoice: 'accepted' | 'refused') {
  const pinia = createPinia();
  setActivePinia(pinia);
  localStorage.setItem('flexstore.cookieChoice', cookieChoice);
  page = mount(Host, { global: { plugins: [vuetify, i18n, createHead(), pinia] } });
  await flushPromises();
}

const keyWords = () => document.head.querySelector('meta[name="keywords"]')?.getAttribute('content');
const analyticsScript = () => document.head.querySelector('script[src*="googletagmanager"]');

describe('useShopPageContentHead', () => {
  beforeEach(() => getShopPageContents.mockReset());
  afterEach(() => {
    page.unmount();
    localStorage.removeItem('flexstore.cookieChoice');
    // Each test has its own head: the tags of the previous one are removed
    document.head.querySelectorAll('script, meta[name="keywords"]').forEach((tag) => tag.remove());
  });

  it('should put the key words of the shop in the head', async () => {
    // Arrange
    getShopPageContents.mockResolvedValue(contentsOf('shop, demo', ''));
    // Act
    await openPage('refused');
    // Assert
    await vi.waitFor(() => expect(keyWords()).toBe('shop, demo'));
  });

  it('should load Google Analytics for a visitor who accepted the cookies', async () => {
    // Arrange
    getShopPageContents.mockResolvedValue(contentsOf('', 'G-ABC123'));
    // Act
    await openPage('accepted');
    // Assert
    await vi.waitFor(() => expect(analyticsScript()?.getAttribute('src')).toContain('id=G-ABC123'));
  });

  it('should not load Google Analytics for a visitor who refused the cookies', async () => {
    // Arrange
    getShopPageContents.mockResolvedValue(contentsOf('', 'G-ABC123'));
    // Act
    await openPage('refused');
    // Assert
    expect(useShopPageContentStore().textOf('GOOGLE_ANALYTICS_ID')).toBe('G-ABC123');
    expect(analyticsScript()).toBeNull();
  });

  it('should not load Google Analytics for an identifier that is not written as such', async () => {
    // Arrange
    getShopPageContents.mockResolvedValue(contentsOf('', "G-1');alert(1);('"));
    // Act
    await openPage('accepted');
    // Assert
    expect(analyticsScript()).toBeNull();
  });
});
