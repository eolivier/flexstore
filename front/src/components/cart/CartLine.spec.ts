import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import CartLine from './CartLine.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';
import type { Item } from '@/models/cart';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const lamp: Item = {
  itemId: 'i-1',
  productId: 'p-1',
  productName: 'Desk lamp',
  productPrice: 40,
  productCurrency: '€',
  productQuantity: 2,
  itemPrice: 80,
};

function mountLine() {
  return mount(CartLine, {
    props: { item: lamp },
    global: { plugins: [vuetify, i18n], stubs: { RouterLink: true } },
  });
}

describe('CartLine.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
  });

  it('a visitor should not see the wishlist button', () => {
    // Act
    const wrapper = mountLine();
    // Assert
    expect(wrapper.find('.wishlist-toggle').exists()).toBe(false);
  });

  it('a signed-in customer should save the product of the line to the wishlist', async () => {
    // Arrange
    const authStore = useAuthStore();
    authStore.token = 'token';
    authStore.currentUser = { id: 'user-1', role: 'USER' } as typeof authStore.currentUser;
    const wrapper = mountLine();
    // Act
    await wrapper.find('.wishlist-toggle').trigger('click');
    await flushPromises();
    // Assert
    expect(mockFetch.mock.calls.some(([url]) => String(url).includes('/api/wishlist/p-1'))).toBe(true);
  });
});
