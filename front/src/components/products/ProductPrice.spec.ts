import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ProductPrice from './ProductPrice.vue';
import { vuetify, i18n } from '@/test/setup';

const lamp = {
  id: 'p-1',
  name: 'Desk lamp',
  sku: 'LAMP-DESK',
  description: 'Warm light',
  category: 'HOME',
  price: 40,
  currency: '€',
};

function mountPrice(product: typeof lamp & { promotionPercentage?: number | null; currentPrice?: number }) {
  return mount(ProductPrice, { props: { product }, global: { plugins: [vuetify, i18n] } });
}

describe('ProductPrice.vue', () => {
  it('a product without promotion should show its price, not struck through', () => {
    // Act
    const wrapper = mountPrice({ ...lamp, promotionPercentage: null, currentPrice: 40 });
    // Assert
    expect(wrapper.text()).toContain('40.00 €');
    expect(wrapper.find('s').exists()).toBe(false);
  });

  it('a product on promotion should show its price to pay, its regular price struck through and the percentage off', () => {
    // Act
    const wrapper = mountPrice({ ...lamp, promotionPercentage: 25, currentPrice: 30 });
    // Assert
    expect(wrapper.text()).toContain('30.00 €');
    expect(wrapper.find('s').text()).toBe('40.00 €');
    expect(wrapper.text()).toContain('-25 %');
  });
});
