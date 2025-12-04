import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createMemoryHistory } from 'vue-router';
import Login from './Login.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

// Mock fetch globally
const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

// Create router with memory history for tests
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/admin', component: { template: '<div>Admin</div>' } },
    { path: '/admin/users', component: { template: '<div>Users</div>' } },
  ],
});

describe('Login.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    router.push('/login');
  });

  const mountComponent = async (query = {}) => {
    if (Object.keys(query).length > 0) {
      await router.push({ path: '/login', query });
    }
    return mount(Login, {
      global: {
        plugins: [vuetify, i18n, router],
      },
    });
  };

  it('renders login form with email and password fields', async () => {
    const wrapper = await mountComponent();

    expect(wrapper.find('input[type="email"]').exists()).toBe(true);
    expect(wrapper.find('input[type="password"]').exists()).toBe(true);
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true);
  });

  it('displays title from i18n', async () => {
    const wrapper = await mountComponent();

    expect(wrapper.text()).toContain('Login');
  });

  it('submit button is disabled when form is invalid', async () => {
    const wrapper = await mountComponent();
    await flushPromises();

    const submitButton = wrapper.find('button[type="submit"]');
    expect(submitButton.attributes('disabled')).toBeDefined();
  });

  it('validates email format', async () => {
    const wrapper = await mountComponent();

    const emailInput = wrapper.find('input[type="email"]');
    await emailInput.setValue('invalid-email');
    await flushPromises();

    // Check that form is still invalid due to bad email format
    const submitButton = wrapper.find('button[type="submit"]');
    expect(submitButton.attributes('disabled')).toBeDefined();
  });

  it('calls login when form is submitted with valid data', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ id: 1, name: 'John', email: 'john@example.com' }),
    });

    const wrapper = await mountComponent();
    const authStore = useAuthStore();

    // Fill in the form
    const emailInput = wrapper.find('input[type="email"]');
    const passwordInput = wrapper.find('input[type="password"]');

    await emailInput.setValue('john@example.com');
    await passwordInput.setValue('password123');
    await flushPromises();

    // Submit the form
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    // Verify fetch was called with correct parameters
    expect(mockFetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/users/login',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'john@example.com', password: 'password123' }),
      })
    );

    // Verify user is authenticated
    expect(authStore.isAuthenticated).toBe(true);
  });

  it('displays error message when login fails', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 401,
    });

    const wrapper = await mountComponent();
    const authStore = useAuthStore();

    // Fill in the form
    const emailInput = wrapper.find('input[type="email"]');
    const passwordInput = wrapper.find('input[type="password"]');

    await emailInput.setValue('john@example.com');
    await passwordInput.setValue('wrongpassword');
    await flushPromises();

    // Submit the form
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    // Verify error is set
    expect(authStore.loginError).toBe('Invalid email or password');

    // Re-render to see error message
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('Invalid email or password');
  });

  it('redirects to admin after successful login', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ id: 1, name: 'John', email: 'john@example.com' }),
    });

    const wrapper = await mountComponent();

    // Fill and submit form
    await wrapper.find('input[type="email"]').setValue('john@example.com');
    await wrapper.find('input[type="password"]').setValue('password123');
    await flushPromises();

    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    // Check router navigation
    expect(router.currentRoute.value.path).toBe('/admin');
  });

  it('redirects to custom path from query params after successful login', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ id: 1, name: 'John', email: 'john@example.com' }),
    });

    const wrapper = await mountComponent({ redirect: '/admin/users' });
    await flushPromises();

    // Fill and submit form
    await wrapper.find('input[type="email"]').setValue('john@example.com');
    await wrapper.find('input[type="password"]').setValue('password123');
    await flushPromises();

    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    // Check router navigation to redirect path
    expect(router.currentRoute.value.path).toBe('/admin/users');
  });
});
