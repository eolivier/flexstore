import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductCard from './ProductCard.vue';
import { useAuthStore } from '@/stores/auth.store';
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

function mountCard(stock: number | null) {
  return mount(ProductCard, { props: { product: lamp, stock }, global: { plugins: [vuetify, i18n, router] } });
}

describe('ProductCard.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it.each([
    { stock: 0, disabled: true },
    { stock: 3, disabled: false },
    { stock: null, disabled: false },
  ])(
    'a product with a stock of $stock should be addable to the cart: $disabled is disabled',
    ({ stock, disabled }) => {
      // Act
      const wrapper = mountCard(stock);
      // Assert
      expect(wrapper.find('button').attributes('disabled') !== undefined).toBe(disabled);
    }
  );

  it.each([
    { stock: 0, offered: true },
    { stock: 3, offered: false },
    { stock: null, offered: false },
  ])('a signed-in customer is offered to be notified with a stock of $stock: $offered', ({ stock, offered }) => {
    // Arrange
    const authStore = useAuthStore();
    authStore.token = 'token';
    authStore.currentUser = { id: 'user-1', role: 'USER' } as typeof authStore.currentUser;
    // Act
    const wrapper = mountCard(stock);
    // Assert
    expect(wrapper.text().includes('Notify me when it is back')).toBe(offered);
  });

  it('should show no stock while the stocks are not loaded', () => {
    // Act
    const wrapper = mountCard(null);
    // Assert
    expect(wrapper.text()).not.toContain('stock');
  });
});
