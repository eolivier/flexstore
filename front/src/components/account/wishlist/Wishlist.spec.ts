import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import Wishlist from './Wishlist.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';
import { stocksApi } from '@/api';

vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  reviewsApi: { getRatings: vi.fn(() => Promise.resolve({ data: [] })) },
  stocksApi: { getStocks: vi.fn() },
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

const floorLamp = { ...deskLamp, id: 'p-2', sku: 'LAMP-FLOOR', name: 'Floor lamp' };

function respondWith(products: object[]) {
  mockFetch.mockImplementation((url: string) => {
    if (url.endsWith('/api/wishlist')) {
      return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve(products) });
    }
    if (url.endsWith('/api/cart/cart-items')) {
      return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve({ items: [] }) });
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
    vi.mocked(stocksApi.getStocks).mockResolvedValue({ data: [] } as never);
    signIn();
  });

  it('should show the products saved by the customer', async () => {
    // Arrange
    respondWith([deskLamp]);
    // Act
    const wrapper = await mountWishlist();
    // Assert
    expect(wrapper.text()).toContain('Desk lamp');
  });

  it('should tell a customer with no saved product where to find some', async () => {
    // Arrange
    respondWith([]);
    // Act
    const wrapper = await mountWishlist();
    // Assert
    expect(wrapper.text()).toContain('Your wishlist is empty');
  });

  it('should report an error when the wishlist could not be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    // Act
    const wrapper = await mountWishlist();
    // Assert
    expect(wrapper.text()).toContain('could not be loaded');
  });

  function stockOf(productId: string, quantity: number) {
    return { productId, quantity };
  }

  it('should add every saved product to the cart in one click', async () => {
    // Arrange
    vi.mocked(stocksApi.getStocks).mockResolvedValue({
      data: [stockOf('p-1', 5), stockOf('p-2', 5)],
    } as never);
    respondWith([deskLamp, floorLamp]);
    const wrapper = await mountWishlist();
    // Act
    await wrapper.find('[data-testid="wishlist-add-all"]').trigger('click');
    await flushPromises();
    // Assert
    const savedProductIds = mockFetch.mock.calls
      .filter(([url]) => url.endsWith('/api/cart/save'))
      .map(([, init]) => JSON.parse(init.body).productId);
    expect(savedProductIds).toEqual(['p-1', 'p-2']);
  });

  it('should leave the products out of stock out of the cart', async () => {
    // Arrange
    vi.mocked(stocksApi.getStocks).mockResolvedValue({
      data: [stockOf('p-1', 5), stockOf('p-2', 0)],
    } as never);
    respondWith([deskLamp, floorLamp]);
    const wrapper = await mountWishlist();
    // Act
    await wrapper.find('[data-testid="wishlist-add-all"]').trigger('click');
    await flushPromises();
    // Assert
    const savedProductIds = mockFetch.mock.calls
      .filter(([url]) => url.endsWith('/api/cart/save'))
      .map(([, init]) => JSON.parse(init.body).productId);
    expect(savedProductIds).toEqual(['p-1']);
  });

  it('should not offer to add anything to the cart when the wishlist is empty', async () => {
    // Arrange
    respondWith([]);
    // Act
    const wrapper = await mountWishlist();
    // Assert
    expect(wrapper.find('[data-testid="wishlist-add-all"]').exists()).toBe(false);
  });
});
