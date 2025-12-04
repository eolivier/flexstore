import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from '@/models/user';

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
  };
});
