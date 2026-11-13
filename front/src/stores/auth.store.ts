import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { AccountStatus, User } from '@/models/user';
import type { ExpressAccountRegistration } from '@/models/account';
import { useLoadingStore } from '@/stores/loading.store';
import { shareWhilePending } from '@/utils/shareWhilePending';
import { jsonRequest } from '@/utils/jsonRequest';
import { logFailure } from '@/utils/logFailure';

export type PasswordChange = 'changed' | 'wrongPassword' | 'error';
// signedIn: the account is created and the customer is signed in; invalidCompany: the company was refused (400)
export type Registration = 'signedIn' | 'emailUsed' | 'invalidCompany' | 'error';
// twoFactorRequired: the password is right, the code of the authenticator app is still needed
export type LoginOutcome = 'signedIn' | 'twoFactorRequired' | 'refused' | 'invalidCredentials' | 'error';
export type TwoFactorVerification = 'signedIn' | 'invalidCode' | 'error';
export type TwoFactorConfirmation = 'confirmed' | 'invalidCode' | 'error';
export type TwoFactorDisabling = 'disabled' | 'wrongPassword' | 'error';

export interface TwoFactorEnrollment {
  secret: string;
  provisioningUri: string;
}

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

interface JsonLoginResponse {
  status: 'SIGNED_IN' | 'TWO_FACTOR_REQUIRED';
  token?: string;
  expiresAt?: string;
  refreshToken?: string;
  refreshExpiresAt?: string;
  user?: User;
  challengeId?: string;
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
  // Set while a sign-in waits for the code of the authenticator app; no session exists yet
  const pendingChallengeId = ref<string | null>(null);
  const loadingStore = useLoadingStore();

  const isAuthenticated = computed(() => currentUser.value !== null && token.value !== null);
  const isAdmin = computed(() => isAuthenticated.value && currentUser.value?.role === 'ADMIN');

  function openSession(session: LoginResponse) {
    token.value = session.token;
    refreshToken.value = session.refreshToken;
    currentUser.value = session.user;
    writeSession(session);
  }

  async function login(email: string, password: string): Promise<LoginOutcome> {
    return signIn('/api/users/login', { email, password }, 'Invalid email or password');
  }

  // Signs in with the ID token that the Google button gave to the browser: the back end verifies it
  async function loginWithGoogle(idToken: string): Promise<LoginOutcome> {
    return signIn('/api/users/login/google', { idToken }, 'Google sign-in failed');
  }

  // The same answers for every way to sign in: a session, a second factor to give, or a refusal
  async function signIn(path: string, credentials: object, invalidMessage: string): Promise<LoginOutcome> {
    loginError.value = null;
    refusedStatus.value = null;
    pendingChallengeId.value = null;
    try {
      const response = await loadingStore.track(fetch(`${apiUrl}${path}`, jsonRequest('POST', credentials)));

      if (response.ok) {
        const result: JsonLoginResponse = await response.json();
        if (result.status === 'TWO_FACTOR_REQUIRED') {
          pendingChallengeId.value = result.challengeId ?? null;
          return 'twoFactorRequired';
        }
        openSession({
          token: result.token!,
          expiresAt: result.expiresAt!,
          refreshToken: result.refreshToken!,
          refreshExpiresAt: result.refreshExpiresAt!,
          user: result.user!,
        });
        return 'signedIn';
      } else if (response.status === 403) {
        // The account is suspended, locked or disabled: the user is told why
        refusedStatus.value = (await response.json()).status ?? null;
        loginError.value = 'Account refused';
        return 'refused';
      } else {
        loginError.value = invalidMessage;
        return 'invalidCredentials';
      }
    } catch (error) {
      loginError.value = 'An error occurred during login';
      logFailure('log in', error);
      return 'error';
    }
  }

  // Completes a challenged sign-in with the 6-digit code of the authenticator app
  async function verifyTwoFactorCode(code: string): Promise<TwoFactorVerification> {
    if (!pendingChallengeId.value) return 'error';
    try {
      const response = await loadingStore.track(
        fetch(
          `${apiUrl}/api/users/login/two-factor-authentication`,
          jsonRequest('POST', { challengeId: pendingChallengeId.value, code })
        )
      );
      if (!response.ok) return response.status === 401 ? 'invalidCode' : 'error';
      const session: LoginResponse = await response.json();
      openSession(session);
      pendingChallengeId.value = null;
      return 'signedIn';
    } catch (error) {
      logFailure('verify the two-factor code', error);
      return 'error';
    }
  }

  // Abandons the challenge and goes back to the credentials step
  function cancelTwoFactorChallenge() {
    pendingChallengeId.value = null;
  }

  // Creates the account of a customer, then signs them in
  async function register(registration: ExpressAccountRegistration): Promise<Registration> {
    try {
      const response = await loadingStore.track(fetch(`${apiUrl}/api/accounts`, jsonRequest('POST', registration)));
      if (response.status === 409) return 'emailUsed';
      if (response.status === 400) return 'invalidCompany';
      if (!response.ok) return 'error';
      const outcome = await login(registration.email, registration.password);
      return outcome === 'signedIn' ? 'signedIn' : 'error';
    } catch (error) {
      logFailure('register', error);
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
        fetch(`${apiUrl}/api/users/refresh`, jsonRequest('POST', { refreshToken: refreshToken.value }))
      );
      if (!response.ok) return false;
      const renewed: RefreshedSession = await response.json();
      token.value = renewed.token;
      refreshToken.value = renewed.refreshToken;
      writeSession({ ...renewed, user: currentUser.value });
      return true;
    } catch (error) {
      logFailure('renew the session', error);
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
      const response = await authFetch(
        `/api/users/${currentUser.value.id}/password`,
        jsonRequest('POST', { currentPassword, newPassword })
      );
      if (response.ok) return 'changed';
      if (response.status === 400) return 'wrongPassword';
      return 'error';
    } catch (error) {
      logFailure('change the password', error);
      return 'error';
    }
  }

  // Starts the enrollment of two-factor authentication: the secret stays pending until confirmed
  async function startTwoFactorEnrollment(): Promise<TwoFactorEnrollment | null> {
    if (!currentUser.value) return null;
    try {
      const response = await authFetch(`/api/users/${currentUser.value.id}/two-factor-authentication/enrollment`, {
        method: 'POST',
      });
      return response.ok ? await response.json() : null;
    } catch (error) {
      logFailure('start the two-factor enrollment', error);
      return null;
    }
  }

  // Confirms the enrollment with the first code of the authenticator app
  async function confirmTwoFactorEnrollment(code: string): Promise<TwoFactorConfirmation> {
    if (!currentUser.value) return 'error';
    try {
      const response = await authFetch(
        `/api/users/${currentUser.value.id}/two-factor-authentication/confirmation`,
        jsonRequest('POST', { code })
      );
      if (response.ok) {
        currentUser.value = { ...currentUser.value, twoFactorEnabled: true };
        return 'confirmed';
      }
      return response.status === 401 ? 'invalidCode' : 'error';
    } catch (error) {
      logFailure('confirm the two-factor enrollment', error);
      return 'error';
    }
  }

  // Disables two-factor authentication, confirmed with the current password
  async function disableTwoFactorAuthentication(currentPassword: string): Promise<TwoFactorDisabling> {
    if (!currentUser.value) return 'error';
    try {
      const response = await authFetch(
        `/api/users/${currentUser.value.id}/two-factor-authentication`,
        jsonRequest('DELETE', { currentPassword })
      );
      if (response.ok) {
        currentUser.value = { ...currentUser.value, twoFactorEnabled: false };
        return 'disabled';
      }
      return response.status === 400 ? 'wrongPassword' : 'error';
    } catch (error) {
      logFailure('disable the two-factor authentication', error);
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
    pendingChallengeId.value = null;
    writeSession(null);
  }

  // Tells the back end to end the session; the user is signed out here whatever its answer
  async function endSessionOnServer(endedRefreshToken: string) {
    try {
      await fetch(`${apiUrl}/api/users/logout`, jsonRequest('POST', { refreshToken: endedRefreshToken }));
    } catch (error) {
      logFailure('log out', error);
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
    pendingChallengeId,
    isAuthenticated,
    isAdmin,
    login,
    loginWithGoogle,
    verifyTwoFactorCode,
    cancelTwoFactorChallenge,
    register,
    markEmailVerified,
    logout,
    authFetch,
    changePassword,
    startTwoFactorEnrollment,
    confirmTwoFactorEnrollment,
    disableTwoFactorAuthentication,
  };
});
