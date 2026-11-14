import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import SimilarProducts from './SimilarProducts.vue';
import type { SimilarProduct } from './similarProduct';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const product = (id: string, name: string) => ({
  id,
  name,
  sku: `SKU-${id}`,
  description: name,
  category: 'HOME',
  price: 35,
  currency: '€',
});
const lamp: SimilarProduct = { product: product('p-1', 'Desk lamp'), stock: 3, rating: null };
const vase: SimilarProduct = { product: product('p-2', 'Blue vase'), stock: 10, rating: null };

function mountSimilar(similarProducts: SimilarProduct[]) {
  return mount(SimilarProducts, {
    props: { similarProducts, addingProductId: 'p-2' },
    global: { plugins: [vuetify, i18n, router] },
  });
}

describe('SimilarProducts.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should show a card for each similar product under the title', () => {
    // Act
    const wrapper = mountSimilar([lamp, vase]);
    // Assert
    expect(wrapper.text()).toContain('In the same category');
    expect(wrapper.findAllComponents({ name: 'ProductCard' })).toHaveLength(2);
  });

  it('should show only the product being added as adding', () => {
    // Act
    const wrapper = mountSimilar([lamp, vase]);
    // Assert
    const cards = wrapper.findAllComponents({ name: 'ProductCard' });
    expect(cards.map((card) => card.props('adding'))).toEqual([false, true]);
  });

  it('should show nothing when there is no similar product', () => {
    // Act
    const wrapper = mountSimilar([]);
    // Assert
    expect(wrapper.text()).toBe('');
  });

  it('should forward the product to add when a card asks to add it', () => {
    // Arrange
    const wrapper = mountSimilar([lamp]);
    // Act
    wrapper.findComponent({ name: 'ProductCard' }).vm.$emit('add', lamp.product);
    // Assert
    expect(wrapper.emitted('add')).toEqual([[lamp.product]]);
  });
});
