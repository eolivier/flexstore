import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { AccountStatus, User } from '@/models/user';
import type { AccountRegistration } from '@/models/account';
import { useLoadingStore } from '@/stores/loading.store';
import { shareWhilePending } from '@/utils/shareWhilePending';

export type PasswordChange = 'changed' | 'wrongPassword' | 'error';
// signedIn: the account is created and the customer is signed in; invalidCompany: the company was refused (400)
export type Registration = 'signedIn' | 'emailUsed' | 'invalidCompany' | 'error';

// The token expires after a few minutes; the refresh token, valid longer, renews it once (see refreshSession)
interface RefreshedSession {
  token: string;
  expiresAt: string;
  refreshToken: string;
  refreshExpiresAt: string;
}

interface LoginResponse extends RefreshedSession {
  user: User;
}

// The session is kept in the tab (sessionStorage): it survives a reload and ends when the tab is closed
const SESSION_KEY = 'flexstore.session';

// Restored while the session can be renewed: an expired token is renewed at the first request that it fails
function readSession(): LoginResponse | null {
  try {
    const stored = sessionStorage.getItem(SESSION_KEY);
    if (!stored) return null;
    const session: LoginResponse = JSON.parse(stored);
    const lastMoment = session.refreshExpiresAt ?? session.expiresAt;
    return new Date(lastMoment) > new Date() ? session : null;
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
  const refreshToken = ref<string | null>(restoredSession?.refreshToken ?? null);
  const loginError = ref<string | null>(null);
  // The status of an account that cannot sign in (SUSPENDED, LOCKED, DISABLED), after a refused sign-in
  const refusedStatus = ref<AccountStatus | null>(null);
  const loadingStore = useLoadingStore();

  const isAuthenticated = computed(() => currentUser.value !== null && token.value !== null);
  const isAdmin = computed(() => isAuthenticated.value && currentUser.value?.role === 'ADMIN');

  async function login(email: string, password: string): Promise<boolean> {
    loginError.value = null;
    refusedStatus.value = null;
    try {
      const response = await loadingStore.track(
        fetch(`${apiUrl}/api/users/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password }),
        })
      );

      if (response.ok) {
        const session: LoginResponse = await response.json();
        token.value = session.token;
        refreshToken.value = session.refreshToken;
        currentUser.value = session.user;
        writeSession(session);
        return true;
      } else if (response.status === 403) {
        // Right password, but a suspended, locked or disabled account: the user is told why
        refusedStatus.value = (await response.json()).status ?? null;
        loginError.value = 'Account refused';
        return false;
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
      const response = await loadingStore.track(
        fetch(`${apiUrl}/api/accounts`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(registration),
        })
      );
      if (response.status === 409) return 'emailUsed';
      if (response.status === 400) return 'invalidCompany';
      if (!response.ok) return 'error';
      return (await login(registration.email, registration.password)) ? 'signedIn' : 'error';
    } catch (error) {
      console.error('Registration error:', error);
      return 'error';
    }
  }

  function sendWithToken(path: string, init: RequestInit): Promise<Response> {
    const headers = new Headers(init.headers);
    if (token.value) {
      headers.set('Authorization', `Bearer ${token.value}`);
    }
    return loadingStore.track(fetch(`${apiUrl}${path}`, { ...init, headers }));
  }

  // Renews the token with the refresh token, which is replaced by a new one. Returns whether it worked
  async function renewSession(): Promise<boolean> {
    if (!refreshToken.value || !currentUser.value) return false;
    try {
      const response = await loadingStore.track(
        fetch(`${apiUrl}/api/users/refresh`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken: refreshToken.value }),
        })
      );
      if (!response.ok) return false;
      const renewed: RefreshedSession = await response.json();
      token.value = renewed.token;
      refreshToken.value = renewed.refreshToken;
      writeSession({ ...renewed, user: currentUser.value });
      return true;
    } catch (error) {
      console.error('Session renewal error:', error);
      return false;
    }
  }

  // Several requests rejected together renew the session once: the refresh token works once
  const refreshSession = shareWhilePending(renewSession);

  // fetch with the token of the signed-in user. A rejected token (it lasts a few minutes) is renewed with the
  // refresh token and the request is sent again, once; when the session cannot be renewed, the user is signed
  // out
  async function authFetch(path: string, init: RequestInit = {}): Promise<Response> {
    const response = await sendWithToken(path, init);
    if (response.status !== 401 || !token.value) return response;
    if (await refreshSession()) {
      const retried = await sendWithToken(path, init);
      if (retried.status === 401) clearSession();
      return retried;
    }
    clearSession();
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

  // The email of the signed-in customer is verified: their account is active, in the tab session too
  function markEmailVerified() {
    if (!currentUser.value) return;
    currentUser.value = { ...currentUser.value, status: 'ACTIVE' };
    const session = readSession();
    if (session) writeSession({ ...session, user: currentUser.value });
  }

  function clearSession() {
    currentUser.value = null;
    token.value = null;
    refreshToken.value = null;
    loginError.value = null;
    writeSession(null);
  }

  // Tells the back end to end the session; the user is signed out here whatever its answer
  async function endSessionOnServer(endedRefreshToken: string) {
    try {
      await fetch(`${apiUrl}/api/users/logout`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: endedRefreshToken }),
      });
    } catch (error) {
      console.error('Logout error:', error);
    }
  }

  function logout() {
    const endedRefreshToken = refreshToken.value;
    clearSession();
    if (endedRefreshToken) endSessionOnServer(endedRefreshToken);
  }

  return {
    currentUser,
    token,
    loginError,
    refusedStatus,
    isAuthenticated,
    isAdmin,
    login,
    register,
    markEmailVerified,
    logout,
    authFetch,
    changePassword,
  };
});
