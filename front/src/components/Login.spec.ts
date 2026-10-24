import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises, type VueWrapper } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createMemoryHistory } from 'vue-router';
import Login from './Login.vue';
import { useAuthStore } from '@/stores/auth.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/admin', component: { template: '<div>Admin</div>' } },
    { path: '/admin/users', component: { template: '<div>Users</div>' } },
    { path: '/user-profile', component: { template: '<div>Profile</div>' } },
  ],
});

// Body of a successful login for a user with the given role
const loginSuccess = (role: 'ADMIN' | 'USER') => ({
  ok: true,
  json: () => Promise.resolve({ token: 'jwt-token', expiresAt: '2030-01-01T00:00:00Z', user: { id: '1', firstName: 'John', lastName: 'Doe', email: 'john@example.com', role } }),
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
    const wrapper = mount(Login, { global: { plugins: [vuetify, i18n, router] } });
    await flushPromises();
    return wrapper;
  };

  const fillForm = async (wrapper: VueWrapper, email: string, password: string) => {
    await wrapper.find('input[type="email"]').setValue(email);
    await wrapper.find('input[type="password"]').setValue(password);
    await flushPromises();
  };

  const submit = async (wrapper: VueWrapper) => {
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
  };

  it('renders login form with email and password fields', async () => {
    // Act
    const wrapper = await mountComponent();
    // Assert
    expect(wrapper.find('input[type="email"]').exists()).toBe(true);
    expect(wrapper.find('input[type="password"]').exists()).toBe(true);
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true);
  });

  it('displays title from i18n', async () => {
    // Act
    const wrapper = await mountComponent();
    // Assert
    expect(wrapper.text()).toContain('Login');
  });

  it('submit button is disabled when form is invalid', async () => {
    // Act
    const wrapper = await mountComponent();
    // Assert
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('validates email format', async () => {
    // Arrange
    const wrapper = await mountComponent();
    // Act
    await wrapper.find('input[type="email"]').setValue('invalid-email');
    await flushPromises();
    // Assert
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('calls login when form is submitted with valid data', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce(loginSuccess('ADMIN'));
    const wrapper = await mountComponent();
    await fillForm(wrapper, 'john@example.com', 'password123');
    // Act
    await submit(wrapper);
    // Assert
    expect(mockFetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/users/login',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'john@example.com', password: 'password123' }),
      })
    );
    expect(useAuthStore().isAuthenticated).toBe(true);
  });

  it('displays error message when login fails', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: false, status: 401 });
    const wrapper = await mountComponent();
    await fillForm(wrapper, 'john@example.com', 'wrongpassword');
    // Act
    await submit(wrapper);
    // Assert
    expect(useAuthStore().loginError).toBe('Invalid email or password');
    expect(wrapper.text()).toContain('Invalid email or password');
  });

  it.each([
    { role: 'ADMIN' as const, query: {}, expectedPath: '/admin' },
    { role: 'USER' as const, query: {}, expectedPath: '/user-profile' },
    { role: 'ADMIN' as const, query: { redirect: '/admin/users' }, expectedPath: '/admin/users' },
  ])('redirects a $role user to $expectedPath after login (query $query)', async ({ role, query, expectedPath }) => {
    // Arrange
    mockFetch.mockResolvedValueOnce(loginSuccess(role));
    const wrapper = await mountComponent(query);
    await fillForm(wrapper, 'john@example.com', 'password123');
    // Act
    await submit(wrapper);
    // Assert
    expect(router.currentRoute.value.path).toBe(expectedPath);
  });

  it('links to the sign-up, keeping where to go back', async () => {
    // Act
    const wrapper = await mountComponent({ redirect: '/cart' });
    // Assert
    const signUp = wrapper.findAll('a').find(link => link.text() === 'Create an account')!;
    expect(signUp.attributes('href')).toBe('/sign-up?redirect=/cart');
  });
});
