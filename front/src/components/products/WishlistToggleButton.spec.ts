import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import WishlistToggleButton from './WishlistToggleButton.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const deskLamp = {
  id: 'p-1',
  sku: 'LAMP-DESK',
  name: 'Desk lamp',
  description: 'Warm light',
  category: 'HOME',
  price: 35,
  currency: '€',
};

function signIn() {
  const authStore = useAuthStore();
  authStore.token = 'token';
  authStore.currentUser = { id: 'user-1', role: 'USER' } as typeof authStore.currentUser;
}

function mountButton() {
  return mount(WishlistToggleButton, { props: { product: deskLamp }, global: { plugins: [vuetify, i18n] } });
}

describe('WishlistToggleButton.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
  });

  it('is hidden for a visitor', () => {
    // Act
    const wrapper = mountButton();
    // Assert
    expect(wrapper.find('button').exists()).toBe(false);
  });

  it('saves the product the first time it is clicked, signed in', async () => {
    // Arrange
    signIn();
    const wrapper = mountButton();
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    const [url, init] = mockFetch.mock.calls[0];
    expect(url).toMatch(/\/api\/wishlist\/p-1$/);
    expect(init.method).toBe('PUT');
    expect(wrapper.find('.mdi-heart').exists()).toBe(true);
  });

  it('removes the product on the second click', async () => {
    // Arrange
    signIn();
    const wrapper = mountButton();
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    const [, init] = mockFetch.mock.calls[1];
    expect(init.method).toBe('DELETE');
    expect(wrapper.find('.mdi-heart-outline').exists()).toBe(true);
  });
});
