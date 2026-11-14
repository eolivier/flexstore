import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductBreadcrumbs from './ProductBreadcrumbs.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

describe('ProductBreadcrumbs.vue', () => {
  it('should show the shop, the category and the name of the product, in that order', () => {
    // Act
    const wrapper = mount(ProductBreadcrumbs, {
      props: { productName: 'Desk lamp', categoryLabel: 'nav.clothing', categoryLink: '/products/home' },
      global: { plugins: [vuetify, i18n, router] },
    });
    // Assert
    const titles = wrapper.findAll('.v-breadcrumbs-item').map((item) => item.text());
    expect(titles).toEqual(['Shop', 'Clothing', 'Desk lamp']);
  });

  it('should link the category to its page', () => {
    // Act
    const wrapper = mount(ProductBreadcrumbs, {
      props: { productName: 'Desk lamp', categoryLabel: 'nav.clothing', categoryLink: '/products/home' },
      global: { plugins: [vuetify, i18n, router] },
    });
    // Assert
    expect(wrapper.findAll('a').map((link) => link.attributes('href'))).toEqual(['/products', '/products/home']);
  });
});
