import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';
import AddedShine from './AddedShine.vue';
import { useAddedToCartSignal } from '@/composables/useAddedToCartSignal';

describe('AddedShine.vue', () => {
  it('should show no reflection before a product is added', () => {
    // Act
    const wrapper = mount(AddedShine, { props: { productId: 'p-1' } });

    // Assert
    expect(wrapper.find('[data-testid="added-shine"]').exists()).toBe(false);
  });

  it('should show a reflection when its own product is added to the cart', async () => {
    // Arrange
    const wrapper = mount(AddedShine, { props: { productId: 'p-1' } });

    // Act
    useAddedToCartSignal().announceAdded('p-1');
    await nextTick();

    // Assert
    expect(wrapper.find('[data-testid="added-shine"]').exists()).toBe(true);
  });

  it('should show no reflection when another product is added to the cart', async () => {
    // Arrange
    const wrapper = mount(AddedShine, { props: { productId: 'p-1' } });

    // Act
    useAddedToCartSignal().announceAdded('p-2');
    await nextTick();

    // Assert
    expect(wrapper.find('[data-testid="added-shine"]').exists()).toBe(false);
  });
});
