import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import PaymentCountdown from './PaymentCountdown.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';

// Awaiting its payment until 10:10
const awaitingOrder = {
  ...twoLinesOrder,
  status: 'AWAITING_PAYMENT' as const,
  paymentDeadline: '2026-10-24T10:10:00Z',
};

function mountCountdown() {
  return mount(PaymentCountdown, { props: { order: awaitingOrder }, global: { plugins: [vuetify, i18n] } });
}

describe('PaymentCountdown.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-10-24T10:00:00Z'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('shows the time left to pay', () => {
    // Act
    const wrapper = mountCountdown();
    // Assert
    expect(wrapper.find('.countdown').text()).toBe('10:00');
    expect(wrapper.text()).toContain('After that, the order is cancelled');
  });

  it('counts the time left down every second', async () => {
    // Arrange
    const wrapper = mountCountdown();
    // Act
    await vi.advanceTimersByTimeAsync(65_000);
    // Assert
    expect(wrapper.find('.countdown').text()).toBe('08:55');
  });

  it('tells the page once the time to pay is over', async () => {
    // Arrange
    const wrapper = mountCountdown();
    // Act
    await vi.advanceTimersByTimeAsync(600_000);
    // Assert
    expect(wrapper.emitted('expired')).toHaveLength(1);
    expect(wrapper.text()).toContain('The time to pay is over');
  });
});
