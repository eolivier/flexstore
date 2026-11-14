import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ProductDetailVisual from './ProductDetailVisual.vue';
import { vuetify, i18n } from '@/test/setup';

vi.mock('@/api', () => ({ apiBaseUrl: 'https://api.example', productImagesApi: {} }));

const lamp = {
  id: 'p-1',
  name: 'Desk lamp',
  sku: 'LAMP-DESK',
  description: 'Warm light',
  category: 'HOME',
  price: 35,
  currency: '€',
};

describe('ProductDetailVisual.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should show the visual of the product on the given tint', () => {
    // Act
    const wrapper = mount(ProductDetailVisual, {
      props: { product: lamp, tint: 'rgb(1, 2, 3)' },
      global: { plugins: [vuetify, i18n] },
    });
    // Assert
    expect(wrapper.find('.detail-visual').attributes('style')).toContain('background: rgb(1, 2, 3)');
    expect(wrapper.findComponent({ name: 'ProductVisual' }).props('productId')).toBe('p-1');
  });
});
