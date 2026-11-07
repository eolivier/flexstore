import { afterEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils';
import { createHead } from '@unhead/vue/client';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent, h } from 'vue';
import { createPinia } from 'pinia';
import { useShopPageContentStore } from '@/stores/shopPageContent.store';
import { useRouteHead } from './useRouteHead';
import { i18n, vuetify } from '@/test/setup';

const Page = { render: () => h('main') };
const Host = defineComponent({
  setup() {
    useRouteHead();
    return () => h('div');
  },
});

let page: VueWrapper;

async function openPage(path: string, shopDescription = '') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/about', component: Page, meta: { title: 'nav.about', description: 'seo.about', indexed: true } },
      { path: '/cart', component: Page },
    ],
  });
  router.push(path);
  await router.isReady();
  const head = createHead();
  const pinia = createPinia();
  useShopPageContentStore(pinia).texts.META_SHOP_DESCRIPTION = shopDescription;
  page = mount(Host, { global: { plugins: [vuetify, i18n, router, head, pinia] } });
  await flushPromises();
}

const metaOf = (name: string) => document.head.querySelector(`meta[name="${name}"]`)?.getAttribute('content');

describe('useRouteHead', () => {
  afterEach(() => page.unmount());

  it('gives an indexed page its title, its description and its canonical address', async () => {
    await openPage('/about');

    await vi.waitFor(() => {
      expect(document.title).toBe('About | Flexstore');
      expect(metaOf('description')).toBe(i18n.global.t('seo.about'));
      expect(metaOf('robots')).toBe('index,follow');
      expect(document.head.querySelector('link[rel="canonical"]')?.getAttribute('href')).toBe(
        `${window.location.origin}/about`
      );
    });
  });

  it('links an indexed page to its address in each language', async () => {
    await openPage('/about');

    await vi.waitFor(() => {
      const alternates = [...document.head.querySelectorAll('link[rel="alternate"]')].map((link) => [
        link.getAttribute('hreflang'),
        link.getAttribute('href'),
      ]);
      expect(alternates).toEqual([
        ['fr', `${window.location.origin}/fr/a-propos`],
        ['en', `${window.location.origin}/en/about`],
        ['x-default', `${window.location.origin}/fr/a-propos`],
      ]);
    });
  });

  it('keeps a page that does not say it is public out of the search engines', async () => {
    await openPage('/cart');

    await vi.waitFor(() => {
      expect(document.title).toBe('Flexstore');
      expect(metaOf('robots')).toBe('noindex,follow');
    });
  });

  it('gives a page without its own description the description written by the administrators', async () => {
    await openPage('/cart', 'The shop of the demo');

    await vi.waitFor(() => {
      expect(metaOf('description')).toBe('The shop of the demo');
    });
  });

  it('keeps the description of a page over the description of the shop', async () => {
    await openPage('/about', 'The shop of the demo');

    await vi.waitFor(() => {
      expect(metaOf('description')).toBe(i18n.global.t('seo.about'));
    });
  });
});
