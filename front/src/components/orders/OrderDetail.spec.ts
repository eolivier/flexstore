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

// The order is read, then the shop asks the provider to verify its payment (no order in the answer) when it
// awaits it, then the order is read again
function answerWith(...orders: object[]) {
  const remainingOrders = [...orders];
  mockFetch.mockImplementation((url: string) => {
    if (String(url).endsWith('/payment/verification')) return Promise.resolve({ ok: true, status: 204 });
    const order = remainingOrders.length > 1 ? remainingOrders.shift() : remainingOrders[0];
    return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve(order) });
  });
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
    answerWith(awaitingPayment, awaitingPayment, paid);
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

  it('verifies the payment of an order awaiting it, even when the customer did not come back from the payment page', async () => {
    // Arrange
    answerWith(awaitingPayment, paid);
    // Act
    const wrapper = await mountDetail(`/orders/${twoLinesOrder.id}`);
    // Assert
    const verification = `/api/orders/${twoLinesOrder.id}/payment/verification`;
    expect(mockFetch.mock.calls.some(([url]) => String(url).endsWith(verification))).toBe(true);
    expect(wrapper.find('[role="timer"]').exists()).toBe(false);
  });

  it('does not verify the payment of an order already paid', async () => {
    // Arrange
    answerWith(paid);
    // Act
    await mountDetail(`/orders/${twoLinesOrder.id}`);
    // Assert
    const verification = `/api/orders/${twoLinesOrder.id}/payment/verification`;
    expect(mockFetch.mock.calls.some(([url]) => String(url).endsWith(verification))).toBe(false);
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
