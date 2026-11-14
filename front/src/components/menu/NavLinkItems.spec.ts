import { afterEach, describe, expect, it } from 'vitest';
import { enableAutoUnmount, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import NavLinkItems from './NavLinkItems.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

const links = [
  { to: '/orders', icon: 'mdi-truck-outline', label: 'nav.orders' },
  { to: '/account/wishlist', icon: 'mdi-heart-outline', label: 'nav.wishlist' },
];

function mountLinks(shown: typeof links) {
  return mount(NavLinkItems, { props: { links: shown }, global: { plugins: [vuetify, i18n, router] } });
}

enableAutoUnmount(afterEach);

describe('NavLinkItems.vue', () => {
  it('should show a translated entry for each link, in order', () => {
    // Act
    const wrapper = mountLinks(links);
    // Assert
    const titles = wrapper.findAll('.v-list-item-title').map((title) => title.text());
    expect(titles).toEqual(['My orders', 'My wishlist']);
  });

  it('should point each entry to the page of its link', () => {
    // Act
    const wrapper = mountLinks(links);
    // Assert
    const addresses = wrapper.findAll('a').map((entry) => entry.attributes('href'));
    expect(addresses).toEqual(['/orders', '/account/wishlist']);
  });

  it('should show nothing when there is no link', () => {
    // Act
    const wrapper = mountLinks([]);
    // Assert
    expect(wrapper.findAll('a')).toHaveLength(0);
  });
});
