import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import OrderSummaryCard from './OrderSummaryCard.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/:any(.*)', component: { template: '<div />' } }] });

describe('OrderSummaryCard.vue', () => {
  it('summarises the order and links to its detail', () => {
    // Act
    const wrapper = mount(OrderSummaryCard, { props: { order: twoLinesOrder }, global: { plugins: [vuetify, i18n, router] } });
    // Assert
    expect(wrapper.find('a').attributes('href')).toBe(`/orders/${twoLinesOrder.id}`);
    expect(wrapper.text()).toContain('Order 46555C9F');
    expect(wrapper.text()).toContain('3 items');
    expect(wrapper.text()).toContain('2 × 5G Smartphone, 1 × Bluetooth Headphones');
    expect(wrapper.text()).toContain('857.00 €');
  });
});
