import { beforeEach, describe, expect, it } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import AppSnackbar from './AppSnackbar.vue';
import { useSnackbar } from '@/composables/useSnackbar';
import { vuetify } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

// The snackbar is rendered in the body by Vuetify
const body = () => document.body.textContent ?? '';
const global = { plugins: [vuetify, router] };
const mountSnackbar = () => mount(AppSnackbar, { global, attachTo: document.body });

describe('AppSnackbar', () => {
  beforeEach(() => {
    useSnackbar().close();
  });

  it('should show the message sent through the composable', async () => {
    // Arrange
    mountSnackbar();
    // Act
    useSnackbar().success('Product saved');
    await flushPromises();
    // Assert
    expect(body()).toContain('Product saved');
  });

  it('should show the action of the message', async () => {
    // Arrange
    mountSnackbar();
    // Act
    useSnackbar().success('Saved', { action: { text: 'Back to the products', to: '/admin/products' } });
    await flushPromises();
    // Assert
    expect(body()).toContain('Back to the products');
  });
});
