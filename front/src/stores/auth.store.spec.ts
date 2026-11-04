import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAuthStore } from './auth.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const createMockUser = () => ({
  id: '1',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john@example.com',
  role: 'USER' as 'USER' | 'ADMIN',
});
// Body of a successful login: the token, the refresh token that renews it, and the user
const loginResponse = (user = createMockUser()) => ({
  token: 'jwt-token',
  expiresAt: '2030-01-01T00:00:00Z',
  refreshToken: 'refresh-token',
  refreshExpiresAt: '2030-01-08T00:00:00Z',
  user,
});
const answer = (status: number, body?: unknown) =>
  mockFetch.mockResolvedValueOnce({ ok: status < 300, status, json: () => Promise.resolve(body) });
// Body of a successful renewal of the session
const renewedSession = {
  token: 'renewed-jwt-token',
  expiresAt: '2030-01-01T00:15:00Z',
  refreshToken: 'renewed-refresh-token',
  refreshExpiresAt: '2030-01-08T00:00:00Z',
};
const loginSucceeds = (user = createMockUser()) =>
  mockFetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(loginResponse(user)) });

// A store with a signed-in user
async function signedIn(user = createMockUser()) {
  loginSucceeds(user);
  const store = useAuthStore();
  await store.login('john@example.com', 'secret');
  return store;
}

describe('auth.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
  });

  describe('initial state', () => {
    it('should not be authenticated, without user nor error', () => {
      // Act
      const store = useAuthStore();
      // Assert
      expect(store.currentUser).toBeNull();
      expect(store.isAuthenticated).toBe(false);
      expect(store.loginError).toBeNull();
    });
  });

  describe('login', () => {
    it('should authenticate user on successful login', async () => {
      // Arrange
      const mockUser = createMockUser();
      loginSucceeds(mockUser);
      const store = useAuthStore();
      // Act
      const loggedIn = await store.login('john@example.com', 'secret');
      // Assert
      expect(loggedIn).toBe(true);
      expect(store.currentUser).toEqual(mockUser);
      expect(store.token).toBe('jwt-token');
      expect(store.isAuthenticated).toBe(true);
      expect(store.isAdmin).toBe(false);
      expect(store.loginError).toBeNull();
    });

    it('should set error on failed login', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce({ ok: false, status: 401 });
      const store = useAuthStore();
      // Act
      const loggedIn = await store.login('john@example.com', 'wrongpassword');
      // Assert
      expect(loggedIn).toBe(false);
      expect(store.isAuthenticated).toBe(false);
      expect(store.loginError).toBe('Invalid email or password');
    });

    it('should handle network errors', async () => {
      // Arrange
      mockFetch.mockRejectedValueOnce(new Error('Network error'));
      const store = useAuthStore();
      // Act
      const loggedIn = await store.login('john@example.com', 'secret');
      // Assert
      expect(loggedIn).toBe(false);
      expect(store.isAuthenticated).toBe(false);
      expect(store.loginError).toBe('An error occurred during login');
    });

    it('should clear previous error on new login attempt', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce({ ok: false, status: 401 });
      const store = useAuthStore();
      await store.login('john@example.com', 'wrong');
      loginSucceeds();
      // Act
      await store.login('john@example.com', 'secret');
      // Assert
      expect(store.loginError).toBeNull();
    });

    it('should call fetch with correct parameters', async () => {
      // Arrange
      loginSucceeds();
      const store = useAuthStore();
      // Act
      await store.login('john@example.com', 'secret');
      // Assert
      expect(mockFetch).toHaveBeenCalledWith('http://localhost:8080/api/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'john@example.com', password: 'secret' }),
      });
    });
  });

  describe('logout', () => {
    it('should clear user, token and error on logout', async () => {
      // Arrange
      const store = await signedIn();
      // Act
      store.logout();
      // Assert
      expect(store.currentUser).toBeNull();
      expect(store.token).toBeNull();
      expect(store.isAuthenticated).toBe(false);
      expect(store.loginError).toBeNull();
    });
  });

  describe('authFetch', () => {
    it('should send the token of the signed-in user', async () => {
      // Arrange
      const store = await signedIn();
      mockFetch.mockResolvedValueOnce({ ok: true, status: 200 });
      // Act
      await store.authFetch('/api/users', { method: 'GET' });
      // Assert
      const [url, init] = mockFetch.mock.lastCall!;
      expect(url).toBe('http://localhost:8080/api/users');
      expect(new Headers(init.headers).get('Authorization')).toBe('Bearer jwt-token');
    });

    it('should sign out when the token is rejected and the session cannot be renewed', async () => {
      // Arrange
      const store = await signedIn();
      answer(401);
      answer(401);
      // Act
      await store.authFetch('/api/users');
      // Assert
      expect(store.isAuthenticated).toBe(false);
      expect(store.token).toBeNull();
    });

    it.each([
      { role: 'ADMIN' as const, isAdmin: true },
      { role: 'USER' as const, isAdmin: false },
    ])('a $role user is admin: $isAdmin', async ({ role, isAdmin }) => {
      // Act
      const store = await signedIn({ ...createMockUser(), role });
      // Assert
      expect(store.isAdmin).toBe(isAdmin);
    });
  });

  describe('changePassword', () => {
    it('should post the passwords for the signed-in user', async () => {
      // Arrange
      const store = await signedIn();
      mockFetch.mockResolvedValueOnce({ ok: true, status: 204 });
      // Act
      const passwordChange = await store.changePassword('secret', 'new-password');
      // Assert
      expect(passwordChange).toBe('changed');
      const [url, init] = mockFetch.mock.lastCall!;
      expect(url).toBe('http://localhost:8080/api/users/1/password');
      expect(init.method).toBe('POST');
      expect(init.body).toBe(JSON.stringify({ currentPassword: 'secret', newPassword: 'new-password' }));
      expect(new Headers(init.headers).get('Authorization')).toBe('Bearer jwt-token');
    });

    it('should report a wrong current password', async () => {
      // Arrange
      const store = await signedIn();
      mockFetch.mockResolvedValueOnce({ ok: false, status: 400 });
      // Act
      const passwordChange = await store.changePassword('wrong', 'new-password');
      // Assert
      expect(passwordChange).toBe('wrongPassword');
    });

    it('should report other errors', async () => {
      // Arrange
      const store = await signedIn();
      mockFetch.mockRejectedValueOnce(new Error('Network error'));
      // Act
      const passwordChange = await store.changePassword('secret', 'new-password');
      // Assert
      expect(passwordChange).toBe('error');
    });

    it('should not call the API when signed out', async () => {
      // Arrange
      const store = useAuthStore();
      // Act
      const passwordChange = await store.changePassword('secret', 'new-password');
      // Assert
      expect(passwordChange).toBe('error');
      expect(mockFetch).not.toHaveBeenCalled();
    });
  });

  describe('session kept on reload', () => {
    // A new store, as when the page is reloaded
    const reloadedStore = () => {
      setActivePinia(createPinia());
      return useAuthStore();
    };

    it('should stay signed in after a reload', async () => {
      // Arrange
      await signedIn();
      // Act
      const store = reloadedStore();
      // Assert
      expect(store.isAuthenticated).toBe(true);
      expect(store.token).toBe('jwt-token');
      expect(store.currentUser).toEqual(createMockUser());
    });

    it.each([
      { what: 'signing out', end: (store: ReturnType<typeof useAuthStore>) => store.logout() },
      {
        what: 'a rejected token',
        end: async (store: ReturnType<typeof useAuthStore>) => {
          answer(401);
          answer(401);
          await store.authFetch('/api/users');
        },
      },
    ])('should be signed out after a reload that follows $what', async ({ end }) => {
      // Arrange
      const store = await signedIn();
      await end(store);
      // Act
      const reloaded = reloadedStore();
      // Assert
      expect(reloaded.isAuthenticated).toBe(false);
    });

    it('should not restore a session whose refresh token expired', () => {
      // Arrange
      sessionStorage.setItem(
        'flexstore.session',
        JSON.stringify({
          ...loginResponse(),
          expiresAt: '2020-01-01T00:00:00Z',
          refreshExpiresAt: '2020-01-08T00:00:00Z',
        })
      );
      // Act
      const store = reloadedStore();
      // Assert
      expect(store.isAuthenticated).toBe(false);
    });

    it('should restore a session whose token expired but whose refresh token did not', () => {
      // Arrange
      sessionStorage.setItem(
        'flexstore.session',
        JSON.stringify({ ...loginResponse(), expiresAt: '2020-01-01T00:00:00Z' })
      );
      // Act
      const store = reloadedStore();
      // Assert
      expect(store.isAuthenticated).toBe(true);
    });
  });

  describe('session renewal', () => {
    const sentRequests = () => mockFetch.mock.calls.map(([url, init]) => ({ url, init }));

    it('should renew the token with the refresh token and send the request again with the new token', async () => {
      // Arrange
      const store = await signedIn();
      answer(401);
      answer(200, renewedSession);
      answer(200, []);
      // Act
      const response = await store.authFetch('/api/orders');
      // Assert
      const [, , refresh, retry] = sentRequests();
      expect(refresh.url).toBe('http://localhost:8080/api/users/refresh');
      expect(JSON.parse(refresh.init.body)).toEqual({ refreshToken: 'refresh-token' });
      expect(new Headers(retry.init.headers).get('Authorization')).toBe('Bearer renewed-jwt-token');
      expect(response.status).toBe(200);
      expect(store.isAuthenticated).toBe(true);
    });

    it('should use the new refresh token for the next renewal', async () => {
      // Arrange
      const store = await signedIn();
      answer(401);
      answer(200, renewedSession);
      answer(200, []);
      await store.authFetch('/api/orders');
      answer(401);
      answer(200, { ...renewedSession, refreshToken: 'second-refresh-token' });
      answer(200, []);
      // Act
      await store.authFetch('/api/orders');
      // Assert
      const refreshes = sentRequests().filter(({ url }) => String(url).endsWith('/api/users/refresh'));
      expect(JSON.parse(refreshes[1].init.body)).toEqual({ refreshToken: 'renewed-refresh-token' });
    });

    it('should keep the renewed session after a reload', async () => {
      // Arrange
      const store = await signedIn();
      answer(401);
      answer(200, renewedSession);
      answer(200, []);
      await store.authFetch('/api/orders');
      // Act
      setActivePinia(createPinia());
      const reloaded = useAuthStore();
      // Assert
      expect(reloaded.token).toBe('renewed-jwt-token');
      expect(reloaded.currentUser).toEqual(createMockUser());
    });

    it('should renew the session once for requests rejected together', async () => {
      // Arrange
      const store = await signedIn();
      mockFetch.mockImplementation(async (url: string, init: RequestInit) => {
        if (url.endsWith('/api/users/refresh'))
          return { ok: true, status: 200, json: async () => renewedSession };
        const renewed = new Headers(init.headers).get('Authorization') === 'Bearer renewed-jwt-token';
        return { ok: renewed, status: renewed ? 200 : 401 };
      });
      // Act
      const responses = await Promise.all([store.authFetch('/api/orders'), store.authFetch('/api/cart')]);
      // Assert
      const refreshes = sentRequests().filter(({ url }) => String(url).endsWith('/api/users/refresh'));
      expect(refreshes).toHaveLength(1);
      expect(responses.map((response) => response.status)).toEqual([200, 200]);
    });

    it('should sign out when the request is still rejected with the renewed token', async () => {
      // Arrange
      const store = await signedIn();
      answer(401);
      answer(200, renewedSession);
      answer(401);
      // Act
      await store.authFetch('/api/orders');
      // Assert
      expect(store.isAuthenticated).toBe(false);
    });

    it('should not try to renew a session that has no refresh token', async () => {
      // Arrange
      const store = await signedIn();
      store.logout();
      sessionStorage.setItem(
        'flexstore.session',
        JSON.stringify({ ...loginResponse(), refreshToken: undefined })
      );
      setActivePinia(createPinia());
      const restored = useAuthStore();
      mockFetch.mockReset();
      answer(401);
      // Act
      await restored.authFetch('/api/orders');
      // Assert
      expect(mockFetch).toHaveBeenCalledTimes(1);
      expect(restored.isAuthenticated).toBe(false);
    });
  });

  describe('logout on the server', () => {
    it('should end the session of the refresh token', async () => {
      // Arrange
      const store = await signedIn();
      answer(204);
      // Act
      store.logout();
      // Assert
      const [url, init] = mockFetch.mock.lastCall!;
      expect(url).toBe('http://localhost:8080/api/users/logout');
      expect(JSON.parse(init.body)).toEqual({ refreshToken: 'refresh-token' });
    });

    it('should sign out here even when the server cannot be reached', async () => {
      // Arrange
      const store = await signedIn();
      mockFetch.mockRejectedValueOnce(new Error('Network error'));
      // Act
      store.logout();
      // Assert
      expect(store.isAuthenticated).toBe(false);
    });

    it('should not call the server when nobody is signed in', () => {
      // Arrange
      const store = useAuthStore();
      // Act
      store.logout();
      // Assert
      expect(mockFetch).not.toHaveBeenCalled();
    });
  });

  describe('register', () => {
    const registration = {
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      password: 'password123',
      phoneNumber: '+33 6 12 34 56 78',
      postalAddress: {
        recipientType: 'PRIVATE_INDIVIDUAL' as const,
        firstName: 'Jane',
        lastName: 'Doe',
        companyName: '',
        vatNumber: '',
        street: '12 rue des Lilas',
        complement: '',
        postalCode: '75011',
        city: 'Paris',
        country: 'France',
      },
    };

    it('should create the account, then sign the customer in', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce({ ok: true, status: 201 });
      loginSucceeds();
      const store = useAuthStore();
      // Act
      const result = await store.register(registration);
      // Assert
      expect(result).toBe('signedIn');
      expect(store.isAuthenticated).toBe(true);
      const [url, init] = mockFetch.mock.calls[0];
      expect(url).toMatch(/\/api\/accounts$/);
      expect(JSON.parse(init.body)).toEqual(registration);
    });

    it.each([
      { status: 409, expected: 'emailUsed' },
      { status: 422, expected: 'error' },
    ])('a registration answered $status is $expected, without signing in', async ({ status, expected }) => {
      // Arrange
      mockFetch.mockResolvedValueOnce({ ok: false, status });
      const store = useAuthStore();
      // Act
      const result = await store.register(registration);
      // Assert
      expect(result).toBe(expected);
      expect(store.isAuthenticated).toBe(false);
    });
  });
});
