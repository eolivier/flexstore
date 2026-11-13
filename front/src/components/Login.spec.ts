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
  json: () =>
    Promise.resolve({
      status: 'SIGNED_IN',
      token: 'jwt-token',
      expiresAt: '2030-01-01T00:00:00Z',
      user: { id: '1', firstName: 'John', lastName: 'Doe', email: 'john@example.com', role },
    }),
});

// Body of a login whose password is right, but still waits for the code of the authenticator app
const twoFactorChallengeRequired = (challengeId = 'challenge-1') => ({
  ok: true,
  json: () => Promise.resolve({ status: 'TWO_FACTOR_REQUIRED', challengeId }),
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

  it('hides the password until the eye icon is clicked, then hides it again', async () => {
    // Arrange
    const wrapper = await mountComponent();
    await wrapper.find('input[type="password"]').setValue('password123');
    // Act
    await wrapper.find('.mdi-eye-outline').trigger('click');
    // Assert
    expect(wrapper.find('input[type="password"]').exists()).toBe(false);
    expect((wrapper.find('input[autocomplete="current-password"]').element as HTMLInputElement).value).toBe(
      'password123'
    );
    // Act
    await wrapper.find('.mdi-eye-off-outline').trigger('click');
    // Assert
    expect(wrapper.find('input[type="password"]').exists()).toBe(true);
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
  ])(
    'redirects a $role user to $expectedPath after login (query $query)',
    async ({ role, query, expectedPath }) => {
      // Arrange
      mockFetch.mockResolvedValueOnce(loginSuccess(role));
      const wrapper = await mountComponent(query);
      await fillForm(wrapper, 'john@example.com', 'password123');
      // Act
      await submit(wrapper);
      // Assert
      expect(router.currentRoute.value.path).toBe(expectedPath);
    }
  );

  it.each([
    { status: 'SUSPENDED', expected: 'Your account is suspended. Contact the shop to know more.' },
    { status: 'LOCKED', expected: 'Your account is locked. Contact the shop to unlock it.' },
    { status: 'DISABLED', expected: 'Your account is disabled.' },
  ])('a $status account is told why it cannot sign in', async ({ status, expected }) => {
    // Arrange
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 403,
      json: () => Promise.resolve({ error: 'refused', status }),
    });
    const wrapper = await mountComponent();
    await fillForm(wrapper, 'jane@example.com', 'password123');
    // Act
    await submit(wrapper);
    // Assert
    expect(wrapper.text()).toContain(expected);
    expect(router.currentRoute.value.path).toBe('/login');
  });

  it('links to the sign-up, keeping where to go back', async () => {
    // Act
    const wrapper = await mountComponent({ redirect: '/cart' });
    // Assert
    const signUp = wrapper.findAll('a').find((link) => link.text() === 'Create an account')!;
    expect(signUp.attributes('href')).toBe('/sign-up?redirect=/cart');
  });

  describe('two-factor challenge', () => {
    it('shows the code step instead of redirecting when a second factor is required', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(twoFactorChallengeRequired());
      const wrapper = await mountComponent();
      await fillForm(wrapper, 'admin@example.com', 'password123');
      // Act
      await submit(wrapper);
      // Assert
      expect(wrapper.text()).toContain('Two-factor authentication');
      expect(wrapper.find('input[type="email"]').exists()).toBe(false);
      expect(router.currentRoute.value.path).toBe('/login');
    });

    it('redirects once the right code is verified', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(twoFactorChallengeRequired());
      const wrapper = await mountComponent();
      await fillForm(wrapper, 'admin@example.com', 'password123');
      await submit(wrapper);
      mockFetch.mockResolvedValueOnce(loginSuccess('ADMIN'));
      // Act
      await wrapper.find('input').setValue('123456');
      await wrapper.find('form').trigger('submit.prevent');
      await flushPromises();
      // Assert
      expect(useAuthStore().isAuthenticated).toBe(true);
      expect(router.currentRoute.value.path).toBe('/admin');
    });

    it('reports a wrong code, without redirecting', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(twoFactorChallengeRequired());
      const wrapper = await mountComponent();
      await fillForm(wrapper, 'admin@example.com', 'password123');
      await submit(wrapper);
      mockFetch.mockResolvedValueOnce({ ok: false, status: 401 });
      // Act
      await wrapper.find('input').setValue('000000');
      await wrapper.find('form').trigger('submit.prevent');
      await flushPromises();
      // Assert
      expect(wrapper.text()).toContain('This code is wrong or has expired.');
      expect(useAuthStore().isAuthenticated).toBe(false);
    });

    it('goes back to the credentials step', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(twoFactorChallengeRequired());
      const wrapper = await mountComponent();
      await fillForm(wrapper, 'admin@example.com', 'password123');
      await submit(wrapper);
      // Act
      await wrapper.findAll('button').find((button) => button.text() === 'Back to sign in')!.trigger('click');
      await flushPromises();
      // Assert
      expect(wrapper.find('input[type="email"]').exists()).toBe(true);
      expect(useAuthStore().pendingChallengeId).toBeNull();
    });
  });
});
