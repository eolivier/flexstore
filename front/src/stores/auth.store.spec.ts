import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAuthStore } from './auth.store';

// Mock fetch globally
const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

// Shared mock user for tests (note: password should not be in API response, but testing current implementation)
const createMockUser = () => ({ id: 1, name: 'John', email: 'john@example.com' });

describe('auth.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  describe('initial state', () => {
    it('should have null currentUser', () => {
      const store = useAuthStore();
      expect(store.currentUser).toBeNull();
    });

    it('should not be authenticated', () => {
      const store = useAuthStore();
      expect(store.isAuthenticated).toBe(false);
    });

    it('should have null loginError', () => {
      const store = useAuthStore();
      expect(store.loginError).toBeNull();
    });
  });

  describe('login', () => {
    it('should authenticate user on successful login', async () => {
      const mockUser = createMockUser();
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockUser),
      });

      const store = useAuthStore();
      const result = await store.login('john@example.com', 'secret');

      expect(result).toBe(true);
      expect(store.currentUser).toEqual(mockUser);
      expect(store.isAuthenticated).toBe(true);
      expect(store.loginError).toBeNull();
    });

    it('should set error on failed login', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 401,
      });

      const store = useAuthStore();
      const result = await store.login('john@example.com', 'wrongpassword');

      expect(result).toBe(false);
      expect(store.currentUser).toBeNull();
      expect(store.isAuthenticated).toBe(false);
      expect(store.loginError).toBe('Invalid email or password');
    });

    it('should handle network errors', async () => {
      mockFetch.mockRejectedValueOnce(new Error('Network error'));

      const store = useAuthStore();
      const result = await store.login('john@example.com', 'secret');

      expect(result).toBe(false);
      expect(store.currentUser).toBeNull();
      expect(store.isAuthenticated).toBe(false);
      expect(store.loginError).toBe('An error occurred during login');
    });

    it('should clear previous error on new login attempt', async () => {
      // First failed login
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 401,
      });

      const store = useAuthStore();
      await store.login('john@example.com', 'wrong');
      expect(store.loginError).toBe('Invalid email or password');

      // Second successful login
      const mockUser = createMockUser();
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockUser),
      });

      await store.login('john@example.com', 'secret');
      expect(store.loginError).toBeNull();
    });

    it('should call fetch with correct parameters', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(createMockUser()),
      });

      const store = useAuthStore();
      await store.login('john@example.com', 'secret');

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/users/login',
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: 'john@example.com', password: 'secret' }),
        }
      );
    });
  });

  describe('logout', () => {
    it('should clear user and error on logout', async () => {
      const mockUser = createMockUser();
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockUser),
      });

      const store = useAuthStore();
      await store.login('john@example.com', 'secret');
      expect(store.isAuthenticated).toBe(true);

      store.logout();

      expect(store.currentUser).toBeNull();
      expect(store.isAuthenticated).toBe(false);
      expect(store.loginError).toBeNull();
    });
  });
});
