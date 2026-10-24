import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from '@/models/user';

export type PasswordChange = 'changed' | 'wrongPassword' | 'error';

interface LoginResponse {
  token: string;
  expiresAt: string;
  user: User;
}

export const useAuthStore = defineStore('auth', () => {
  const apiUrl = import.meta.env.VITE_API_URL;
  const currentUser = ref<User | null>(null);
  // Kept in memory only (not in localStorage) so that a script injected in the page cannot read it after a reload
  const token = ref<string | null>(null);
  const loginError = ref<string | null>(null);

  const isAuthenticated = computed(() => currentUser.value !== null && token.value !== null);
  const isAdmin = computed(() => isAuthenticated.value && currentUser.value?.role === 'ADMIN');

  async function login(email: string, password: string): Promise<boolean> {
    loginError.value = null;
    try {
      const response = await fetch(`${apiUrl}/api/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        const session: LoginResponse = await response.json();
        token.value = session.token;
        currentUser.value = session.user;
        return true;
      } else {
        loginError.value = 'Invalid email or password';
        return false;
      }
    } catch (error) {
      loginError.value = 'An error occurred during login';
      console.error('Login error:', error);
      return false;
    }
  }

  // fetch with the token of the signed-in user; an expired or rejected token signs the user out
  async function authFetch(path: string, init: RequestInit = {}): Promise<Response> {
    const headers = new Headers(init.headers);
    if (token.value) {
      headers.set('Authorization', `Bearer ${token.value}`);
    }
    const response = await fetch(`${apiUrl}${path}`, { ...init, headers });
    if (response.status === 401 && token.value) {
      logout();
    }
    return response;
  }

  // Changes the password of the signed-in user, after checking the current one
  async function changePassword(currentPassword: string, newPassword: string): Promise<PasswordChange> {
    if (!currentUser.value) return 'error';
    try {
      const response = await authFetch(`/api/users/${currentUser.value.id}/password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPassword, newPassword }),
      });
      if (response.ok) return 'changed';
      if (response.status === 400) return 'wrongPassword';
      return 'error';
    } catch (error) {
      console.error('Change password error:', error);
      return 'error';
    }
  }

  function logout() {
    currentUser.value = null;
    token.value = null;
    loginError.value = null;
  }

  return {
    currentUser,
    token,
    loginError,
    isAuthenticated,
    isAdmin,
    login,
    logout,
    authFetch,
    changePassword,
  };
});
