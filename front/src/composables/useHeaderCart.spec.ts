import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { defineComponent, h } from 'vue';
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { useHeaderCart } from './useHeaderCart';
import { useSnackbar } from './useSnackbar';
import { useCartStore } from '@/stores/cart.store';
import { i18n } from '@/test/setup';

const Host = defineComponent({
  setup() {
    const { cartCount } = useHeaderCart();
    return () => h('span', cartCount.value);
  },
});

function mountHost() {
  const cartStore = useCartStore();
  cartStore.fetchCartItems = vi.fn();
  const wrapper = mount(Host, { global: { plugins: [i18n] } });
  return { wrapper, cartStore };
}

enableAutoUnmount(afterEach);

describe('useHeaderCart', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    useSnackbar().close();
  });

  it('should load the cart when the header is shown', () => {
    // Act
    const { cartStore } = mountHost();
    // Assert
    expect(cartStore.fetchCartItems).toHaveBeenCalledTimes(1);
  });

  it('should count the quantities of the products of the cart', async () => {
    // Arrange
    const { wrapper, cartStore } = mountHost();
    const cartItems = {
      items: [
        { itemId: 'i-1', productQuantity: 2 },
        { itemId: 'i-2', productQuantity: 3 },
      ],
    };
    // Act
    cartStore.cartItems = cartItems as never;
    await flushPromises();
    // Assert
    expect(wrapper.text()).toBe('5');
  });

  it('should count zero products while the cart is not loaded', () => {
    // Act
    const { wrapper } = mountHost();
    // Assert
    expect(wrapper.text()).toBe('0');
  });

  it('should tell once that the items of the account were merged into the cart', async () => {
    // Arrange
    const { cartStore } = mountHost();
    // Act
    cartStore.accountItemsMerged = true;
    await flushPromises();
    // Assert
    expect(useSnackbar().message.value?.text).toBe('The items saved in your account have been added to your cart.');
    expect(cartStore.accountItemsMerged).toBe(false);
  });
});
