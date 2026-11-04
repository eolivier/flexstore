import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import Wishlist from './Wishlist.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  reviewsApi: { getRatings: vi.fn(() => Promise.resolve({ data: [] })) },
  stocksApi: { getStocks: vi.fn(() => Promise.resolve({ data: [] })) },
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

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

function respondWith(products: object[]) {
  mockFetch.mockImplementation((url: string) => {
    if (url.endsWith('/api/wishlist')) {
      return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve(products) });
    }
    return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve([]) });
  });
}

async function mountWishlist() {
  const wrapper = mount(Wishlist, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('Wishlist.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    signIn();
  });

  it('shows the products saved by the customer', async () => {
    // Arrange
    respondWith([deskLamp]);
    // Act
    const wrapper = await mountWishlist();
    // Assert
    expect(wrapper.text()).toContain('Desk lamp');
  });

  it('tells a customer with no saved product where to find some', async () => {
    // Arrange
    respondWith([]);
    // Act
    const wrapper = await mountWishlist();
    // Assert
    expect(wrapper.text()).toContain('Your wishlist is empty');
  });

  it('reports an error when the wishlist could not be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    // Act
    const wrapper = await mountWishlist();
    // Assert
    expect(wrapper.text()).toContain('could not be loaded');
  });
});
