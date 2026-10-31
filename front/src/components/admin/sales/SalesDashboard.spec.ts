import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import SalesDashboard from './SalesDashboard.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

const dashboard = {
  firstDay: '2026-10-29',
  lastDay: '2026-10-31',
  currency: '€',
  revenue: 300,
  orderCount: 4,
  days: [
    { day: '2026-10-29', orderCount: 0, revenue: 0 },
    { day: '2026-10-30', orderCount: 1, revenue: 100 },
    { day: '2026-10-31', orderCount: 3, revenue: 200 },
  ],
  bestSellers: [
    { productId: 'chair', productName: 'Chair', unitsSold: 3, revenue: 300 },
    { productId: 'lamp', productName: 'Desk lamp', unitsSold: 1, revenue: 35 },
  ],
};

async function mountDashboard() {
  const wrapper = mount(SalesDashboard, { global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('SalesDashboard.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(dashboard) });
  });

  it('shows the revenue, the orders and the average basket', async () => {
    // Act
    const wrapper = await mountDashboard();
    // Assert
    const figures = wrapper.findAll('[data-test="figure"]').map((figure) => figure.text());
    expect(figures).toHaveLength(3);
    expect(figures[1]).toContain('4');
    expect(figures[0]).toContain('300');
    expect(figures[2]).toContain('75');
  });

  it('draws a bar per day, the empty days included', async () => {
    // Act
    const wrapper = await mountDashboard();
    // Assert
    const heights = wrapper.findAll('[data-test="bar"]').map((bar) => bar.attributes('style'));
    expect(heights).toEqual(['height: 0%;', 'height: 33%;', 'height: 100%;']);
  });

  it('lists the best sellers, the best first', async () => {
    // Act
    const wrapper = await mountDashboard();
    // Assert
    const names = wrapper.findAll('[data-test="best-seller"]').map((row) => row.find('td').text());
    expect(names).toEqual(['Chair', 'Desk lamp']);
  });

  it('tells when the sales could not be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    // Act
    const wrapper = await mountDashboard();
    // Assert
    expect(wrapper.text()).toContain('The sales could not be loaded.');
  });
});
