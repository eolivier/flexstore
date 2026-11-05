import { afterEach, describe, expect, it } from 'vitest';
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils';
import { createHead } from '@unhead/vue/client';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent, h } from 'vue';
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

async function openPage(path: string) {
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
  page = mount(Host, { global: { plugins: [vuetify, i18n, router, head] } });
  await flushPromises();
}

const metaOf = (name: string) => document.head.querySelector(`meta[name="${name}"]`)?.getAttribute('content');

describe('useRouteHead', () => {
  afterEach(() => page.unmount());

  it('gives an indexed page its title, its description and its canonical address', async () => {
    await openPage('/about');
    await flushPromises();

    expect(document.title).toBe('About | Flexstore');
    expect(metaOf('description')).toBe(i18n.global.t('seo.about'));
    expect(metaOf('robots')).toBe('index,follow');
    expect(document.head.querySelector('link[rel="canonical"]')?.getAttribute('href')).toBe(
      `${window.location.origin}/about`
    );
  });

  it('links an indexed page to its address in each language', async () => {
    await openPage('/about');
    await flushPromises();

    const alternates = [...document.head.querySelectorAll('link[rel="alternate"]')].map((link) => [
      link.getAttribute('hreflang'),
      link.getAttribute('href'),
    ]);
    expect(alternates).toEqual([
      ['fr', `${window.location.origin}/fr/about`],
      ['en', `${window.location.origin}/en/about`],
      ['x-default', `${window.location.origin}/fr/about`],
    ]);
  });

  it('keeps a page that does not say it is public out of the search engines', async () => {
    await openPage('/cart');
    await flushPromises();

    expect(document.title).toBe('Flexstore');
    expect(metaOf('robots')).toBe('noindex,follow');
  });
});
