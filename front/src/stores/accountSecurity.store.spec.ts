import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import type { User } from '@/models/user';
import { useAuthStore } from './auth.store';
import { useAccountSecurityStore } from './accountSecurity.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const createMockUser = (): User => ({
  id: '1',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john@example.com',
  role: 'USER',
});

// Body of a successful login: the token, the refresh token that renews it, and the user
const loginResponse = (user = createMockUser()) => ({
  status: 'SIGNED_IN',
  token: 'jwt-token',
  expiresAt: '2030-01-01T00:00:00Z',
  refreshToken: 'refresh-token',
  refreshExpiresAt: '2030-01-08T00:00:00Z',
  user,
});

// The session store, with a signed-in user
async function signedIn(user = createMockUser()) {
  mockFetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(loginResponse(user)) });
  const store = useAuthStore();
  await store.login('john@example.com', 'secret');
  return store;
}

describe('accountSecurity.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    localStorage.clear();
  });

  describe('changePassword', () => {
    it('should post the passwords for the signed-in user', async () => {
      // Arrange
      await signedIn();
      const store = useAccountSecurityStore();
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
      await signedIn();
      const store = useAccountSecurityStore();
      mockFetch.mockResolvedValueOnce({ ok: false, status: 400 });
      // Act
      const passwordChange = await store.changePassword('wrong', 'new-password');
      // Assert
      expect(passwordChange).toBe('wrongPassword');
    });

    it('should report other errors', async () => {
      // Arrange
      await signedIn();
      const store = useAccountSecurityStore();
      mockFetch.mockRejectedValueOnce(new Error('Network error'));
      // Act
      const passwordChange = await store.changePassword('secret', 'new-password');
      // Assert
      expect(passwordChange).toBe('error');
    });

    it('should not call the API when signed out', async () => {
      // Arrange
      const store = useAccountSecurityStore();
      // Act
      const passwordChange = await store.changePassword('secret', 'new-password');
      // Assert
      expect(passwordChange).toBe('error');
      expect(mockFetch).not.toHaveBeenCalled();
    });
  });

  describe('two-factor authentication of the signed-in customer', () => {
    it('should start an enrollment and read its secret and provisioning URI', async () => {
      // Arrange
      await signedIn(createMockUser());
      const store = useAccountSecurityStore();
      const enrollment = { secret: 'ABCDEFGH', provisioningUri: 'otpauth://totp/Flexstore:john' };
      mockFetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(enrollment) });
      // Act
      const result = await store.startTwoFactorEnrollment();
      // Assert
      expect(result).toEqual(enrollment);
      const [url, init] = mockFetch.mock.lastCall!;
      expect(url).toBe('http://localhost:8080/api/users/1/two-factor-authentication/enrollment');
      expect(init.method).toBe('POST');
    });

    it('should confirm the enrollment and mark two-factor authentication enabled', async () => {
      // Arrange
      const authStore = await signedIn(createMockUser());
      const store = useAccountSecurityStore();
      mockFetch.mockResolvedValueOnce({ ok: true, status: 204 });
      // Act
      const outcome = await store.confirmTwoFactorEnrollment('123456');
      // Assert
      expect(outcome).toBe('confirmed');
      expect(authStore.currentUser?.twoFactorEnabled).toBe(true);
    });

    it('should report a wrong confirmation code', async () => {
      // Arrange
      const authStore = await signedIn(createMockUser());
      const store = useAccountSecurityStore();
      mockFetch.mockResolvedValueOnce({ ok: false, status: 401 });
      // Act
      const outcome = await store.confirmTwoFactorEnrollment('000000');
      // Assert
      expect(outcome).toBe('invalidCode');
      expect(authStore.currentUser?.twoFactorEnabled).toBeFalsy();
    });

    it('should disable two-factor authentication with the current password', async () => {
      // Arrange
      const authStore = await signedIn({ ...createMockUser(), twoFactorEnabled: true });
      const store = useAccountSecurityStore();
      mockFetch.mockResolvedValueOnce({ ok: true, status: 204 });
      // Act
      const outcome = await store.disableTwoFactorAuthentication('secret');
      // Assert
      expect(outcome).toBe('disabled');
      expect(authStore.currentUser?.twoFactorEnabled).toBe(false);
      const [, init] = mockFetch.mock.lastCall!;
      expect(init.method).toBe('DELETE');
    });

    it('should report a wrong current password when disabling', async () => {
      // Arrange
      const authStore = await signedIn({ ...createMockUser(), twoFactorEnabled: true });
      const store = useAccountSecurityStore();
      mockFetch.mockResolvedValueOnce({ ok: false, status: 400 });
      // Act
      const outcome = await store.disableTwoFactorAuthentication('wrong');
      // Assert
      expect(outcome).toBe('wrongPassword');
      expect(authStore.currentUser?.twoFactorEnabled).toBe(true);
    });
  });
});
