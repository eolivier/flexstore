import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ProductRating from './ProductRating.vue';
import { vuetify, i18n } from '@/test/setup';

describe('ProductRating.vue', () => {
  it.each([
    { reviewCount: 1, expected: '4.0 (1 review)' },
    { reviewCount: 12, expected: '4.0 (12 reviews)' },
  ])('reads "$expected"', ({ reviewCount, expected }) => {
    // Act
    const wrapper = mount(ProductRating, {
      props: { rating: { average: 4, reviewCount } },
      global: { plugins: [vuetify, i18n] },
    });
    // Assert
    expect(wrapper.get('.text-caption').text()).toBe(expected);
  });
});
