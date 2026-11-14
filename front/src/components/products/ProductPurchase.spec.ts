import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductPurchase from './ProductPurchase.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

function mountPurchase(stock: number | null, quantityInCart: number) {
  return mount(ProductPurchase, {
    props: { modelValue: 2, productId: 'p-1', stock, adding: false, quantityInCart },
    global: { plugins: [vuetify, i18n, router] },
  });
}

function addButtonOf(wrapper: ReturnType<typeof mountPurchase>) {
  return wrapper.findAll('button').find((button) => button.text() === 'Add to cart');
}

describe('ProductPurchase.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it.each([
    { stock: 0, disabled: true },
    { stock: 3, disabled: false },
    { stock: null, disabled: false },
  ])('a stock of $stock should disable the add button: $disabled', ({ stock, disabled }) => {
    // Act
    const wrapper = mountPurchase(stock, 0);
    // Assert
    expect(addButtonOf(wrapper)?.attributes('disabled') !== undefined).toBe(disabled);
  });

  it('should offer a signed-in customer to be notified when the product is out of stock', () => {
    // Arrange
    const authStore = useAuthStore();
    authStore.token = 'token';
    authStore.currentUser = { id: 'user-1', role: 'USER' } as typeof authStore.currentUser;
    // Act
    const wrapper = mountPurchase(0, 0);
    // Assert
    expect(wrapper.text()).toContain('Notify me when it is back');
  });

  it('should ask to add the product when the customer clicks on the add button', async () => {
    // Arrange
    const wrapper = mountPurchase(3, 0);
    // Act
    await addButtonOf(wrapper)?.trigger('click');
    // Assert
    expect(wrapper.emitted('add')).toHaveLength(1);
  });

  it('should link to the cart when the product is already in it', () => {
    // Act
    const wrapper = mountPurchase(3, 2);
    // Assert
    expect(wrapper.text()).toContain('2 already in your cart');
    expect(wrapper.find('a[href="/cart"]').exists()).toBe(true);
  });

  it('should not link to the cart when the product is not in it', () => {
    // Act
    const wrapper = mountPurchase(3, 0);
    // Assert
    expect(wrapper.text()).toContain('Not in your cart yet');
    expect(wrapper.find('a[href="/cart"]').exists()).toBe(false);
  });

  it('should show the quantity to order', () => {
    // Act
    const wrapper = mountPurchase(3, 0);
    // Assert
    expect(wrapper.find('input').element.value).toBe('2');
  });
});
