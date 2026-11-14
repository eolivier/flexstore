import { afterEach, describe, expect, it, vi } from 'vitest';
import { enableAutoUnmount, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import HeaderNavLinks from './HeaderNavLinks.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

function mountLinks() {
  return mount(HeaderNavLinks, { global: { plugins: [vuetify, i18n, router] }, attachTo: document.body });
}

// Each menu is attached to the page, for its overlay: it is unmounted after its test
enableAutoUnmount(afterEach);

describe('HeaderNavLinks.vue', () => {
  it('should link the home and the about pages', () => {
    // Act
    const wrapper = mountLinks();
    // Assert
    const addresses = wrapper.findAll('a').map((link) => link.attributes('href'));
    expect(addresses).toEqual(['/', '/about']);
  });

  it('should offer the shop categories in the shop menu', async () => {
    // Arrange
    const wrapper = mountLinks();
    const shopButton = wrapper.findAll('button').find((button) => button.text() === 'Shop');
    // Act
    await shopButton?.trigger('mouseenter');
    // Assert
    // The menu opens on hover, after a short delay
    await vi.waitFor(() => expect(document.body.textContent).toContain('All products'));
    expect(document.body.textContent).toContain('Clothing');
  });
});
