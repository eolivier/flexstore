import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductSummary from './ProductSummary.vue';
import type { Rating } from '@/stores/reviews.store';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const lamp = {
  id: 'p-1',
  name: 'Desk lamp',
  sku: 'LAMP-DESK',
  description: 'Warm light',
  category: 'HOME',
  price: 35,
  currency: '€',
};

function mountSummary(stock: number | null, rating: Rating | null) {
  return mount(ProductSummary, {
    props: { product: lamp, categoryLabel: 'nav.clothing', categoryLink: '/products/home', stock, rating },
    global: { plugins: [vuetify, i18n, router] },
  });
}

describe('ProductSummary.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should show the name, the description and the link to the category of the product', () => {
    // Act
    const wrapper = mountSummary(null, null);
    // Assert
    expect(wrapper.find('h1').text()).toBe('Desk lamp');
    expect(wrapper.text()).toContain('Warm light');
    expect(wrapper.find('a.category-link').attributes('href')).toBe('/products/home');
  });

  it('should show the stock and the rating when they are known', () => {
    // Act
    const wrapper = mountSummary(3, { average: 4.5, reviewCount: 2 });
    // Assert
    expect(wrapper.text()).toContain('Only 3 left in stock');
    expect(wrapper.find('[data-test="product-rating"]').exists()).toBe(true);
  });

  it('should show neither the stock nor the rating while they are unknown', () => {
    // Act
    const wrapper = mountSummary(null, null);
    // Assert
    expect(wrapper.text()).not.toContain('stock');
    expect(wrapper.find('[data-test="product-rating"]').exists()).toBe(false);
  });
});
