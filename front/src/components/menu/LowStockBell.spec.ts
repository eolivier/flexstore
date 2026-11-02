import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import LowStockBell from './LowStockBell.vue';
import { useProductsStore } from '@/stores/products.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const page = { template: '<div />' };
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: page },
    { path: '/admin/products', component: page },
    { path: '/admin/products/:id/edit', component: page },
  ],
});

const deskLamp = {
  id: 'p-1',
  sku: 'LAMP',
  name: 'Desk lamp',
  description: '',
  category: 'ACCESSORIES',
  currency: 'EUR',
};

function lowStocksAre(lowStocks: unknown[]) {
  mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(lowStocks) });
}

async function mountBell() {
  const wrapper = mount(LowStockBell, { global: { plugins: [vuetify, i18n, router] }, attachTo: document.body });
  await flushPromises();
  return wrapper;
}

// Each bell is attached to the page, for its menu: it is unmounted after its test
enableAutoUnmount(afterEach);

const lowStockCalls = () => mockFetch.mock.calls.filter(([url]) => /\/api\/admin\/stocks\/low$/.test(url));

describe('LowStockBell.vue', () => {
  beforeEach(async () => {
    setActivePinia(createPinia());
    useProductsStore().products = [deskLamp];
    mockFetch.mockReset();
    await router.push('/');
  });

  it('counts the products that run low', async () => {
    // Arrange
    lowStocksAre([{ productId: 'p-1', quantity: 2, threshold: 5 }]);
    // Act
    const wrapper = await mountBell();
    // Assert
    expect(wrapper.find('.v-badge__badge').text()).toBe('1');
  });

  it('lists the products that run low, with their units left', async () => {
    // Arrange
    lowStocksAre([{ productId: 'p-1', quantity: 2, threshold: 5 }]);
    const wrapper = await mountBell();
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    expect(document.body.textContent).toContain('Desk lamp');
    expect(document.body.textContent).toContain('2 left (alert at 5)');
  });

  it('shows no count when no product runs low', async () => {
    // Arrange
    lowStocksAre([]);
    // Act
    const wrapper = await mountBell();
    // Assert
    expect(wrapper.find('.v-badge__badge').isVisible()).toBe(false);
  });

  it('reads the low stocks again at each page opened', async () => {
    // Arrange
    lowStocksAre([]);
    await mountBell();
    // Act
    await router.push('/admin/products');
    await flushPromises();
    // Assert
    expect(lowStockCalls()).toHaveLength(2);
  });
});
