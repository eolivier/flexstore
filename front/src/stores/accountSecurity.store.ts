import { defineStore } from 'pinia';
import { useAuthStore } from '@/stores/auth.store';
import { jsonRequest } from '@/utils/jsonRequest';
import { logFailure } from '@/utils/logFailure';

export type PasswordChange = 'changed' | 'wrongPassword' | 'error';
export type TwoFactorConfirmation = 'confirmed' | 'invalidCode' | 'error';
export type TwoFactorDisabling = 'disabled' | 'wrongPassword' | 'error';

export interface TwoFactorEnrollment {
  secret: string;
  provisioningUri: string;
}

// The security of the account of the signed-in user: their password and their two-factor authentication
export const useAccountSecurityStore = defineStore('accountSecurity', () => {
  const authStore = useAuthStore();

  // Two-factor authentication is on or off for the user of the session, whatever the page that told it
  function markTwoFactorEnabled(enabled: boolean) {
    if (authStore.currentUser) authStore.currentUser = { ...authStore.currentUser, twoFactorEnabled: enabled };
  }

  // Changes the password of the signed-in user, after checking the current one
  async function changePassword(currentPassword: string, newPassword: string): Promise<PasswordChange> {
    if (!authStore.currentUser) return 'error';
    try {
      const response = await authStore.authFetch(
        `/api/users/${authStore.currentUser.id}/password`,
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
    if (!authStore.currentUser) return null;
    try {
      const response = await authStore.authFetch(
        `/api/users/${authStore.currentUser.id}/two-factor-authentication/enrollment`,
        { method: 'POST' }
      );
      return response.ok ? await response.json() : null;
    } catch (error) {
      logFailure('start the two-factor enrollment', error);
      return null;
    }
  }

  // Confirms the enrollment with the first code of the authenticator app
  async function confirmTwoFactorEnrollment(code: string): Promise<TwoFactorConfirmation> {
    if (!authStore.currentUser) return 'error';
    try {
      const response = await authStore.authFetch(
        `/api/users/${authStore.currentUser.id}/two-factor-authentication/confirmation`,
        jsonRequest('POST', { code })
      );
      if (response.ok) {
        markTwoFactorEnabled(true);
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
    if (!authStore.currentUser) return 'error';
    try {
      const response = await authStore.authFetch(
        `/api/users/${authStore.currentUser.id}/two-factor-authentication`,
        jsonRequest('DELETE', { currentPassword })
      );
      if (response.ok) {
        markTwoFactorEnabled(false);
        return 'disabled';
      }
      return response.status === 400 ? 'wrongPassword' : 'error';
    } catch (error) {
      logFailure('disable the two-factor authentication', error);
      return 'error';
    }
  }

  return { changePassword, startTwoFactorEnrollment, confirmTwoFactorEnrollment, disableTwoFactorAuthentication };
});
