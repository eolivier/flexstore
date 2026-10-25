import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import StockBadge from './StockBadge.vue';
import { vuetify, i18n } from '@/test/setup';

describe('StockBadge.vue', () => {
  it.each([
    { quantity: 0, expected: 'Out of stock' },
    { quantity: 1, expected: 'Only 1 left in stock' },
    { quantity: 5, expected: 'Only 5 left in stock' },
    { quantity: 6, expected: 'In stock' },
  ])('$quantity units read "$expected"', ({ quantity, expected }) => {
    // Act
    const wrapper = mount(StockBadge, { props: { quantity }, global: { plugins: [vuetify, i18n] } });
    // Assert
    expect(wrapper.text()).toBe(expected);
  });
});
