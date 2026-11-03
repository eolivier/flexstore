import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import AdminOrderDetail from './AdminOrderDetail.vue';
import { twoLinesAdminOrder } from '@/components/orders/orderFixture';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

// The order as read by GET, and as advanced by POST
function respondWith(order: object, advancedOrder: object = order) {
  mockFetch.mockImplementation((_url: string, init?: RequestInit) => {
    const body = init?.method === 'POST' ? advancedOrder : order;
    return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve(body) });
  });
}

async function mountAdminOrderDetail() {
  const wrapper = mount(AdminOrderDetail, {
    props: { id: twoLinesAdminOrder.id },
    global: { plugins: [vuetify, i18n, router] },
    attachTo: document.body,
  });
  await flushPromises();
  return wrapper;
}

describe('AdminOrderDetail.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('reads the order as an administrator', async () => {
    // Arrange
    respondWith(twoLinesAdminOrder);
    // Act
    await mountAdminOrderDetail();
    // Assert
    const [url] = mockFetch.mock.calls[0];
    expect(url).toMatch(new RegExp(`/api/admin/orders/${twoLinesAdminOrder.id}$`));
  });

  it('shows the order with its customer, its lines and its addresses', async () => {
    // Arrange
    respondWith(twoLinesAdminOrder);
    // Act
    const wrapper = await mountAdminOrderDetail();
    // Assert
    expect(wrapper.text()).toContain('Order 46555C9F');
    expect(wrapper.text()).toContain('John Smith');
    expect(wrapper.find('a[href="mailto:john.smith@example.com"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('5G Smartphone');
    expect(wrapper.text()).toContain('12 rue des Lilas');
  });

  it('shows the statuses the order went through, with their date', async () => {
    // Arrange
    respondWith(twoLinesAdminOrder);
    // Act
    const wrapper = await mountAdminOrderDetail();
    // Assert
    const historyItems = wrapper.findAll('.v-timeline-item').map((item) => item.text());
    expect(historyItems).toHaveLength(3);
    expect(historyItems[0]).toContain('Awaiting payment');
    expect(historyItems[0]).toContain('October 24, 2026');
    expect(historyItems[2]).toContain('Processing');
  });

  it('moves the order to its next status', async () => {
    // Arrange
    const shippedOrder = { ...twoLinesAdminOrder, status: 'SHIPPED' };
    respondWith(twoLinesAdminOrder, shippedOrder);
    const wrapper = await mountAdminOrderDetail();
    respondWith(shippedOrder);
    // Act
    await wrapper
      .findAll('button')
      .find((button) => button.text() === 'Move to: Shipped')!
      .trigger('click');
    await flushPromises();
    const shipWithoutTracking = [...document.querySelectorAll('button')].find(
      (button) => button.textContent?.trim() === 'Ship without tracking'
    )!;
    shipWithoutTracking.click();
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('Move to: Delivered');
  });

  it('shows a refunded order as such, without its steps', async () => {
    // Arrange
    respondWith({ ...twoLinesAdminOrder, status: 'REFUNDED' });
    // Act
    const wrapper = await mountAdminOrderDetail();
    // Assert
    expect(wrapper.text()).toContain('This order was cancelled and refunded.');
    expect(wrapper.text()).not.toContain('Cancel and refund');
  });

  it('offers the invoice and the credit note of a refunded order', async () => {
    // Arrange
    respondWith({ ...twoLinesAdminOrder, status: 'REFUNDED' });
    // Act
    const wrapper = await mountAdminOrderDetail();
    // Assert
    expect(wrapper.text()).toContain('Download the invoice');
    expect(wrapper.text()).toContain('Download the credit note');
  });

  it('offers no credit note for an order that is not refunded', async () => {
    // Arrange
    respondWith({ ...twoLinesAdminOrder, status: 'PAID' });
    // Act
    const wrapper = await mountAdminOrderDetail();
    // Assert
    expect(wrapper.text()).toContain('Download the invoice');
    expect(wrapper.text()).not.toContain('Download the credit note');
  });

  it('says that the payment provider refused the refund', async () => {
    // Arrange
    mockFetch.mockImplementation((_url: string, init?: RequestInit) =>
      Promise.resolve(
        init?.method === 'POST'
          ? { ok: false, status: 502 }
          : { ok: true, status: 200, json: () => Promise.resolve(twoLinesAdminOrder) }
      )
    );
    const wrapper = await mountAdminOrderDetail();
    const refundButton = wrapper.findComponent({ name: 'RefundOrderButton' });
    // Act
    refundButton.vm.$emit('refund', twoLinesAdminOrder);
    await flushPromises();
    // Assert
    const [url] = mockFetch.mock.calls.find(([, init]) => init?.method === 'POST')!;
    expect(url).toMatch(new RegExp(`/api/admin/orders/${twoLinesAdminOrder.id}/refund$`));
    expect(wrapper.text()).toContain('The payment provider refused the refund: the order is unchanged.');
  });

  it('says that the order does not exist', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 404 });
    // Act
    const wrapper = await mountAdminOrderDetail();
    // Assert
    expect(wrapper.find('.v-alert').exists()).toBe(true);
    expect(wrapper.text()).not.toContain('Order 46555C9F');
  });
});
