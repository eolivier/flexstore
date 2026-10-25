import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import CreateProduct from './CreateProduct.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

describe('CreateProduct.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('the cancel button goes back to the list of products', () => {
    // Act
    const wrapper = mount(CreateProduct, { global: { plugins: [vuetify, i18n, router] } });
    // Assert
    const cancel = wrapper.findAll('a').find((link) => link.text() === 'Cancel')!;
    expect(cancel.attributes('href')).toBe('/admin/products');
  });
});
