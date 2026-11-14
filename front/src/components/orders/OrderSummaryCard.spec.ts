import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import type { JsonOrder } from '@/generated';
import { createMemoryHistory, createRouter } from 'vue-router';
import OrderSummaryCard from './OrderSummaryCard.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

function mountCard(order: JsonOrder) {
  return mount(OrderSummaryCard, { props: { order }, global: { plugins: [vuetify, i18n, router] } });
}

describe('OrderSummaryCard.vue', () => {
  it('should summarise the order and link to its detail', () => {
    // Act
    const wrapper = mount(OrderSummaryCard, {
      props: { order: twoLinesOrder },
      global: { plugins: [vuetify, i18n, router] },
    });
    // Assert
    expect(wrapper.find('a').attributes('href')).toBe(`/orders/${twoLinesOrder.id}`);
    expect(wrapper.text()).toContain('Order 46555C9F');
    expect(wrapper.text()).toContain('3 items');
    expect(wrapper.text()).toContain('2 × 5G Smartphone, 1 × Bluetooth Headphones');
    expect(wrapper.text()).toContain('857.00 €');
    expect(wrapper.find('[aria-current="step"]').text()).toBe('Processing');
  });

  it('shows no countdown for an order that is not awaiting its payment', () => {
    // Act
    const wrapper = mountCard(twoLinesOrder);
    // Assert
    expect(wrapper.find('.countdown').exists()).toBe(false);
  });

  describe('awaiting its payment until 10:10', () => {
    const awaitingOrder = {
      ...twoLinesOrder,
      status: 'AWAITING_PAYMENT' as const,
      paymentDeadline: '2026-10-24T10:10:00Z',
    };

    beforeEach(() => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2026-10-24T10:00:00Z'));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('shows the time left to pay, counted down every second', async () => {
      // Act
      const wrapper = mountCard(awaitingOrder);
      await vi.advanceTimersByTimeAsync(65_000);
      // Assert
      expect(wrapper.find('.countdown').text()).toBe('08:55');
    });

    it('tells the list once the time to pay is over', async () => {
      // Arrange
      const wrapper = mountCard(awaitingOrder);
      // Act
      await vi.advanceTimersByTimeAsync(600_000);
      // Assert
      expect(wrapper.emitted('expired')).toHaveLength(1);
    });
  });
});
