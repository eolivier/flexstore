import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import ProductNotFound from './ProductNotFound.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

function mountNotFound(failed: boolean) {
  return mount(ProductNotFound, { props: { failed }, global: { plugins: [vuetify, i18n, router] } });
}

describe('ProductNotFound.vue', () => {
  it('should tell that the product does not exist when it did not fail to load', () => {
    // Act
    const wrapper = mountNotFound(false);
    // Assert
    expect(wrapper.text()).toContain('Product not found');
    expect(wrapper.text()).toContain('Back to the shop');
    expect(wrapper.text()).not.toContain('Retry');
  });

  it('should tell that the product could not be loaded and offer to retry when it failed to load', () => {
    // Act
    const wrapper = mountNotFound(true);
    // Assert
    expect(wrapper.text()).toContain('The products could not be loaded.');
    expect(wrapper.text()).toContain('Retry');
  });

  it('should ask to retry when the customer clicks on the retry button', async () => {
    // Arrange
    const wrapper = mountNotFound(true);
    const retryButton = wrapper.findAll('button').find((button) => button.text() === 'Retry');
    // Act
    await retryButton?.trigger('click');
    // Assert
    expect(wrapper.emitted('retry')).toHaveLength(1);
  });
});
