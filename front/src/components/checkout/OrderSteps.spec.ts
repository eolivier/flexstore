import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import OrderSteps from './OrderSteps.vue';
import { vuetify, i18n } from '@/test/setup';

function mountSteps(current: 'cart' | 'delivery' | 'payment') {
  const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/:p(.*)*', component: {} }] });
  return mount(OrderSteps, { props: { current }, global: { plugins: [vuetify, i18n, router] } });
}

describe('OrderSteps.vue', () => {
  it('should select only the current step', () => {
    // Arrange / Act
    const wrapper = mountSteps('delivery');
    // Assert
    const selected = wrapper.findAll('[aria-current="step"]');
    expect(selected).toHaveLength(1);
    expect(selected[0].text()).toBe('Delivery');
  });

  it('should lead back to the cart from the delivery step', () => {
    // Arrange / Act
    const wrapper = mountSteps('delivery');
    // Assert
    const links = wrapper.findAll('a');
    expect(links).toHaveLength(1);
    expect(links[0].attributes('href')).toBe('/cart');
  });

  it('should have no link on the first step', () => {
    // Arrange / Act
    const wrapper = mountSteps('cart');
    // Assert
    expect(wrapper.findAll('a')).toHaveLength(0);
  });
});
