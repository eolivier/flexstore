import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductCard from './ProductCard.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const lamp = {
  id: 'p-1',
  name: 'Desk lamp',
  description: 'Warm light',
  category: 'HOME',
  price: 35,
  currency: '€',
};

function mountCard(stock: number | null) {
  return mount(ProductCard, { props: { product: lamp, stock }, global: { plugins: [vuetify, i18n, router] } });
}

describe('ProductCard.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
    i18n.global.locale.value = 'en';
  });

  it('shows the name and the description in the language of the customer', () => {
    // Arrange
    const frenchLamp = { language: 'fr', name: 'Lampe de bureau', description: 'Lumière chaude' };
    const translatedLamp = { ...lamp, translations: [frenchLamp] };
    i18n.global.locale.value = 'fr';
    // Act
    const wrapper = mount(ProductCard, {
      props: { product: translatedLamp, stock: 3 },
      global: { plugins: [vuetify, i18n, router] },
    });
    // Assert
    expect(wrapper.text()).toContain('Lampe de bureau');
    expect(wrapper.text()).toContain('Lumière chaude');
  });

  it.each([
    { stock: 0, disabled: true },
    { stock: 3, disabled: false },
    { stock: null, disabled: false },
  ])(
    'a product with a stock of $stock can be added to the cart: $disabled is disabled',
    ({ stock, disabled }) => {
      // Act
      const wrapper = mountCard(stock);
      // Assert
      expect(wrapper.find('button').attributes('disabled') !== undefined).toBe(disabled);
    }
  );

  it('shows no stock while the stocks are not loaded', () => {
    // Act
    const wrapper = mountCard(null);
    // Assert
    expect(wrapper.text()).not.toContain('stock');
  });
});
