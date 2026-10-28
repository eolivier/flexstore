import { describe, expect, it } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import RefundOrderButton from './RefundOrderButton.vue';
import type { JsonOrder } from '@/generated';
import { twoLinesOrder } from '@/components/orders/orderFixture';
import { vuetify, i18n } from '@/test/setup';

function mountRefundOrderButton(status: JsonOrder['status']) {
  return mount(RefundOrderButton, {
    props: { order: { ...twoLinesOrder, status }, refunding: false },
    global: { plugins: [vuetify, i18n] },
    attachTo: document.body,
  });
}

describe('RefundOrderButton.vue', () => {
  it.each([
    { status: 'PAID' as const, shown: true },
    { status: 'PROCESSING' as const, shown: true },
    { status: 'AWAITING_PAYMENT' as const, shown: false },
    { status: 'SHIPPED' as const, shown: false },
    { status: 'REFUNDED' as const, shown: false },
  ])('an order $status offers the refund: $shown', ({ status, shown }) => {
    // Act
    const wrapper = mountRefundOrderButton(status);
    // Assert
    expect(wrapper.text().includes('Cancel and refund')).toBe(shown);
  });

  it('refunds the order once the administrator confirms it', async () => {
    // Arrange
    const wrapper = mountRefundOrderButton('PAID');
    await wrapper.find('button').trigger('click');
    await flushPromises();
    const confirm = [...document.querySelectorAll('button')].find((button) => button.textContent === 'Confirm')!;
    // Act
    confirm.click();
    await flushPromises();
    // Assert
    expect(wrapper.emitted('refund')).toHaveLength(1);
    wrapper.unmount();
  });
});
