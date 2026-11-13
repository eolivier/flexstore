import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import CartItemUnitPrice from './CartItemUnitPrice.vue';
import type { Item } from '@/models/cart';

const lamp: Item = {
  itemId: 'i-1',
  productId: 'p-1',
  productName: 'Desk lamp',
  productPrice: 40,
  regularPrice: 40,
  productCurrency: '€',
  productQuantity: 2,
  itemPrice: 80,
};

function mountUnitPrice(item: Item) {
  return mount(CartItemUnitPrice, { props: { item } });
}

describe('CartItemUnitPrice.vue', () => {
  it('an item without promotion should show its unit price, not struck through', () => {
    // Act
    const wrapper = mountUnitPrice({ ...lamp, promotionPercentage: null });
    // Assert
    expect(wrapper.text()).toContain('40.00 € each');
    expect(wrapper.find('s').exists()).toBe(false);
  });

  it('an item on promotion should show its price to pay, its regular price struck through and the percentage off', () => {
    // Act
    const wrapper = mountUnitPrice({ ...lamp, productPrice: 30, promotionPercentage: 25 });
    // Assert
    expect(wrapper.text()).toContain('30.00 € each');
    expect(wrapper.find('s').text()).toBe('40.00 €');
    expect(wrapper.text()).toContain('-25 %');
  });
});
