import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AdminOrders from './AdminOrders.vue';
import { twoLinesOrder } from '@/components/orders/orderFixture';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const deliveredOrder = { ...twoLinesOrder, id: 'b7c1d2e3-0000', status: 'DELIVERED' as const };

function respondWith(orders: object[]) {
  mockFetch.mockImplementation((_url: string, init?: RequestInit) => {
    const body = init?.method === 'POST' ? { ...twoLinesOrder, status: 'SHIPPED' } : orders;
    return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve(body) });
  });
}

async function mountAdminOrders() {
  const wrapper = mount(AdminOrders, { global: { plugins: [vuetify, i18n] } });
  await flushPromises();
  return wrapper;
}

describe('AdminOrders.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('lists the orders of the customers with their recipient and their status', async () => {
    // Arrange
    respondWith([twoLinesOrder, deliveredOrder]);
    // Act
    const wrapper = await mountAdminOrders();
    // Assert
    expect(wrapper.text()).toContain('2 orders');
    expect(wrapper.text()).toContain('Jane Doe');
    expect(wrapper.text()).toContain('Processing');
  });

  it.each([
    { order: twoLinesOrder, expected: 'Move to: Shipped' },
    { order: deliveredOrder, expected: 'Last status' },
    {
      order: { ...twoLinesOrder, status: 'AWAITING_PAYMENT' as const },
      expected: 'Awaiting payment',
    },
  ])('an order $order.status offers "$expected"', async ({ order, expected }) => {
    // Arrange
    respondWith([order]);
    // Act
    const wrapper = await mountAdminOrders();
    // Assert
    expect(wrapper.text()).toContain(expected);
  });

  it('moving an order shows its next status', async () => {
    // Arrange
    respondWith([twoLinesOrder]);
    const wrapper = await mountAdminOrders();
    // Act
    await wrapper
      .findAll('button')
      .find((button) => button.text() === 'Move to: Shipped')!
      .trigger('click');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('Shipped');
    expect(wrapper.text()).toContain('Move to: Delivered');
  });
});
