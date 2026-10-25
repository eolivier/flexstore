import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ProductVisual from './ProductVisual.vue';
import { useProductImagesStore } from '@/stores/productImages.store';
import { vuetify, i18n } from '@/test/setup';

vi.mock('@/api', () => ({ apiBaseUrl: 'https://api.example', productImagesApi: {} }));

function mountVisual(productId: string) {
  return mount(ProductVisual, {
    props: { productId, category: 'HOME', format: 'small', iconSize: 64 },
    global: { plugins: [vuetify, i18n] },
  });
}

describe('ProductVisual.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    useProductImagesStore().productIds = new Set(['lamp']);
  });

  it('shows the image of a product in the given format', () => {
    // Act
    const wrapper = mountVisual('lamp');
    // Assert
    expect(wrapper.find('img').attributes('src')).toBe(
      'https://api.example/api/products/lamp/image?format=small&v=0'
    );
  });

  it('shows the icon of its category for a product without image', () => {
    // Act
    const wrapper = mountVisual('mug');
    // Assert
    expect(wrapper.find('img').exists()).toBe(false);
    expect(wrapper.find('.v-icon').exists()).toBe(true);
  });
});
