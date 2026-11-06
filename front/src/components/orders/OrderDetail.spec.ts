import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import OrderDetail from './OrderDetail.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const awaitingPayment = {
  ...twoLinesOrder,
  status: 'AWAITING_PAYMENT',
  paymentDeadline: '2999-01-01T00:00:00Z',
};
const paid = { ...twoLinesOrder, status: 'PAID' };

function answerWith(...orders: object[]) {
  orders.forEach((order) =>
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(order) })
  );
  mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(orders[orders.length - 1]) });
}

async function mountDetail(path: string) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/orders/:id', component: { template: '<div />' } }],
  });
  await router.push(path);
  const wrapper = mount(OrderDetail, {
    props: { id: twoLinesOrder.id },
    global: { plugins: [vuetify, i18n, router] },
  });
  await flushPromises();
  return wrapper;
}

describe('OrderDetail.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    vi.useFakeTimers();
  });
  afterEach(() => vi.useRealTimers());

  it('shows the time left to pay an order awaiting its payment', async () => {
    // Arrange
    answerWith(awaitingPayment);
    // Act
    const wrapper = await mountDetail(`/orders/${twoLinesOrder.id}`);
    // Assert
    expect(wrapper.find('[role="timer"]').exists()).toBe(true);
    expect(wrapper.text()).not.toContain('Your payment is being confirmed');
  });

  it('waits for the confirmation instead of offering to pay again when the customer just paid', async () => {
    // Arrange
    answerWith(awaitingPayment, paid);
    // Act
    const wrapper = await mountDetail(`/orders/${twoLinesOrder.id}?paymentReturned=true`);
    const whileAwaiting = wrapper.text();
    const timerWhileAwaiting = wrapper.find('[role="timer"]').exists();
    await vi.advanceTimersByTimeAsync(2000);
    await flushPromises();
    // Assert
    expect(whileAwaiting).toContain('Your payment is being confirmed');
    expect(timerWhileAwaiting).toBe(false);
    expect(wrapper.text()).not.toContain('Your payment is being confirmed');
    expect(wrapper.find('[role="timer"]').exists()).toBe(false);
  });

  it('shows the time left again when the payment is still not confirmed after the checks', async () => {
    // Arrange
    answerWith(awaitingPayment);
    const wrapper = await mountDetail(`/orders/${twoLinesOrder.id}?paymentReturned=true`);
    // Act
    await vi.advanceTimersByTimeAsync(20000);
    await flushPromises();
    // Assert
    expect(wrapper.text()).not.toContain('Your payment is being confirmed');
    expect(wrapper.find('[role="timer"]').exists()).toBe(true);
  });
});
