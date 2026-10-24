import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from '@/models/user';

export type ChangePasswordResult = 'changed' | 'wrongPassword' | 'error';

export const useAuthStore = defineStore('auth', () => {
  const apiUrl = import.meta.env.VITE_API_URL;
  const currentUser = ref<User | null>(null);
  const loginError = ref<string | null>(null);

  const isAuthenticated = computed(() => currentUser.value !== null);

  async function login(email: string, password: string): Promise<boolean> {
    loginError.value = null;
    try {
      const response = await fetch(`${apiUrl}/api/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        currentUser.value = await response.json();
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

  // Changes the password of the signed-in user, after checking the current one
  async function changePassword(currentPassword: string, newPassword: string): Promise<ChangePasswordResult> {
    if (!currentUser.value) return 'error';
    try {
      const response = await fetch(`${apiUrl}/api/users/${currentUser.value.id}/password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPassword, newPassword }),
      });
      if (response.ok) return 'changed';
      if (response.status === 401) return 'wrongPassword';
      return 'error';
    } catch (error) {
      console.error('Change password error:', error);
      return 'error';
    }
  }

  function logout() {
    currentUser.value = null;
    loginError.value = null;
  }

  return {
    currentUser,
    loginError,
    isAuthenticated,
    login,
    logout,
    changePassword,
  };
});
