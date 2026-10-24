import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from '@/models/user';
import type { AccountRegistration } from '@/models/account';
import { useLoadingStore } from '@/stores/loading.store';

export type PasswordChange = 'changed' | 'wrongPassword' | 'error';
// signedIn: the account is created and the customer is signed in
export type Registration = 'signedIn' | 'emailUsed' | 'error';

interface LoginResponse {
  token: string;
  expiresAt: string;
  user: User;
}

// The session is kept in the tab (sessionStorage): it survives a reload and ends when the tab is closed
const SESSION_KEY = 'flexstore.session';

function readSession(): LoginResponse | null {
  try {
    const stored = sessionStorage.getItem(SESSION_KEY);
    if (!stored) return null;
    const session: LoginResponse = JSON.parse(stored);
    return new Date(session.expiresAt) > new Date() ? session : null;
  } catch {
    return null;
  }
}

function writeSession(session: LoginResponse | null) {
  try {
    if (session) {
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
    } else {
      sessionStorage.removeItem(SESSION_KEY);
    }
  } catch {
    // Storage unavailable: the session lasts until the page is reloaded
  }
}

export const useAuthStore = defineStore('auth', () => {
  const apiUrl = import.meta.env.VITE_API_URL;
  // Restored from the tab when the page is reloaded, unless the token has expired
  const restoredSession = readSession();
  const currentUser = ref<User | null>(restoredSession?.user ?? null);
  const token = ref<string | null>(restoredSession?.token ?? null);
  const loginError = ref<string | null>(null);
  const loadingStore = useLoadingStore();

  const isAuthenticated = computed(() => currentUser.value !== null && token.value !== null);
  const isAdmin = computed(() => isAuthenticated.value && currentUser.value?.role === 'ADMIN');

  async function login(email: string, password: string): Promise<boolean> {
    loginError.value = null;
    try {
      const response = await loadingStore.track(fetch(`${apiUrl}/api/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      }));

      if (response.ok) {
        const session: LoginResponse = await response.json();
        token.value = session.token;
        currentUser.value = session.user;
        writeSession(session);
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

  // Creates the account of a customer, then signs them in
  async function register(registration: AccountRegistration): Promise<Registration> {
    try {
      const response = await loadingStore.track(fetch(`${apiUrl}/api/accounts`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registration),
      }));
      if (response.status === 409) return 'emailUsed';
      if (!response.ok) return 'error';
      return (await login(registration.email, registration.password)) ? 'signedIn' : 'error';
    } catch (error) {
      console.error('Registration error:', error);
      return 'error';
    }
  }

  // fetch with the token of the signed-in user; an expired or rejected token signs the user out
  async function authFetch(path: string, init: RequestInit = {}): Promise<Response> {
    const headers = new Headers(init.headers);
    if (token.value) {
      headers.set('Authorization', `Bearer ${token.value}`);
    }
    const response = await loadingStore.track(fetch(`${apiUrl}${path}`, { ...init, headers }));
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
    writeSession(null);
  }

  return {
    currentUser,
    token,
    loginError,
    isAuthenticated,
    isAdmin,
    login,
    register,
    logout,
    authFetch,
    changePassword,
  };
});
