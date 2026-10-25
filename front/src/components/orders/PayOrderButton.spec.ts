import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import PayOrderButton from './PayOrderButton.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';
import { openExternalPage } from '@/utils/navigation';

vi.mock('@/utils/navigation', () => ({ openExternalPage: vi.fn() }));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const awaitingPaymentOrder = { ...twoLinesOrder, status: 'AWAITING_PAYMENT' as const };

function respondWith(status: number) {
  const paymentSession = {
    checkoutUrl: 'https://checkout.stripe.com/c/pay/cs_1',
    outcome: 'PENDING',
  };
  mockFetch.mockResolvedValue({
    ok: status < 300,
    status,
    json: () => Promise.resolve(paymentSession),
  });
}

async function pay() {
  const wrapper = mount(PayOrderButton, {
    props: { order: awaitingPaymentOrder },
    global: { plugins: [vuetify, i18n] },
  });
  await wrapper.find('button').trigger('click');
  await flushPromises();
  return wrapper;
}

describe('PayOrderButton.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    vi.mocked(openExternalPage).mockReset();
  });

  it('opens the checkout page of the payment provider', async () => {
    // Arrange
    respondWith(201);
    // Act
    await pay();
    // Assert
    expect(openExternalPage).toHaveBeenCalledWith('https://checkout.stripe.com/c/pay/cs_1');
  });

  it('tells the customer when the payment could not start', async () => {
    // Arrange
    respondWith(500);
    // Act
    const wrapper = await pay();
    // Assert
    expect(wrapper.text()).toContain('The payment could not start');
    expect(openExternalPage).not.toHaveBeenCalled();
  });
});
