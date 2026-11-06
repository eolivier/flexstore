import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import OrderConfirmation from './OrderConfirmation.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const order = {
  id: '46555c9f-4e92-4a00-88cc-03c88440cdb9',
  placedAt: '2026-10-24T10:00:00Z',
  status: 'PROCESSING',
  lines: [
    { productId: '4', productName: '5G Smartphone', unitPrice: 399, quantity: 1, linePrice: 399 },
    {
      productId: '8',
      productName: 'Bluetooth Headphones',
      unitPrice: 59,
      quantity: 1,
      linePrice: 59,
    },
  ],
  totalPrice: 458,
  currency: '€',
};

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

async function mountConfirmation() {
  const wrapper = mount(OrderConfirmation, {
    props: { id: order.id },
    global: { plugins: [vuetify, i18n, router] },
  });
  await flushPromises();
  return wrapper;
}

describe('OrderConfirmation.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(order) });
  });

  it('thanks the customer and shows the order number, item count and total', async () => {
    // Act
    const wrapper = await mountConfirmation();
    // Assert
    expect(wrapper.text()).toContain('Thank you for your order!');
    expect(wrapper.text()).toContain('46555C9F');
    expect(wrapper.text()).toContain('2 items');
    expect(wrapper.text()).toContain('458.00 €');
  });

  it('links the order number and the button to the order detail, told that the customer just paid', async () => {
    // Act
    const wrapper = await mountConfirmation();
    // Assert
    const detailLinks = wrapper.findAll(`a[href="/orders/${order.id}?paymentReturned=true"]`);
    expect(detailLinks.map((link) => link.text())).toEqual(['46555C9F', 'See the order details']);
  });

  it('waits for the confirmation of a payment, then shows the order as paid', async () => {
    // Arrange
    vi.useFakeTimers();
    const awaitingPayment = { ...order, status: 'AWAITING_PAYMENT' };
    const paid = { ...order, status: 'PAID' };
    mockFetch
      .mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(awaitingPayment) })
      .mockResolvedValueOnce({ ok: true, status: 204 })
      .mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(awaitingPayment) })
      .mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(paid) });
    const wrapper = await mountConfirmation();
    const whileAwaiting = wrapper.text();
    // Act
    await vi.advanceTimersByTimeAsync(2000);
    await flushPromises();
    // Assert
    expect(whileAwaiting).toContain('Your payment is being confirmed');
    expect(wrapper.text()).not.toContain('Your payment is being confirmed');
    vi.useRealTimers();
  });

  it('asks the shop to verify the payment with the provider when the order awaits it', async () => {
    // Arrange
    mockFetch.mockImplementation((url: string) =>
      Promise.resolve(
        String(url).endsWith('/payment/verification')
          ? { ok: true, status: 204 }
          : { ok: true, status: 200, json: () => Promise.resolve({ ...order, status: 'AWAITING_PAYMENT' }) }
      )
    );
    // Act
    await mountConfirmation();
    // Assert
    const verification = `/api/orders/${order.id}/payment/verification`;
    expect(mockFetch.mock.calls.filter(([url]) => String(url).endsWith(verification))).toHaveLength(1);
  });

  it('does not verify the payment of an order already paid', async () => {
    // Act
    await mountConfirmation();
    // Assert
    const verification = `/api/orders/${order.id}/payment/verification`;
    expect(mockFetch.mock.calls.some(([url]) => String(url).endsWith(verification))).toBe(false);
  });
});
