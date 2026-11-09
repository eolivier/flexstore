import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import HeaderCartButton from './HeaderCartButton.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/', component: { template: '<div />' } }],
});

function mountCartButton(count: number) {
  return mount(HeaderCartButton, { props: { count }, global: { plugins: [vuetify, i18n, router] } });
}

async function nextFrame() {
  await vi.advanceTimersByTimeAsync(20);
  await flushPromises();
}

enableAutoUnmount(afterEach);

describe('HeaderCartButton.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers({ toFake: ['requestAnimationFrame'] });
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('shows the number of products in the cart', () => {
    // Act
    const wrapper = mountCartButton(3);
    // Assert
    expect(wrapper.find('.v-badge__badge').text()).toBe('3');
  });

  it('magnifies when a product is added', async () => {
    // Arrange
    const wrapper = mountCartButton(1);
    // Act
    await wrapper.setProps({ count: 2 });
    await nextFrame();
    // Assert
    expect(wrapper.find('.cart-badge').classes()).toContain('magnified');
  });

  it('does not magnify when a product is removed', async () => {
    // Arrange
    const wrapper = mountCartButton(2);
    // Act
    await wrapper.setProps({ count: 1 });
    await nextFrame();
    // Assert
    expect(wrapper.find('.cart-badge').classes()).not.toContain('magnified');
  });

  it('stops magnifying at the end of the animation', async () => {
    // Arrange
    const wrapper = mountCartButton(1);
    await wrapper.setProps({ count: 2 });
    await nextFrame();
    // Act
    await wrapper.find('.cart-badge').trigger('animationend');
    // Assert
    expect(wrapper.find('.cart-badge').classes()).not.toContain('magnified');
  });
});
