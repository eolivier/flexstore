import { defineStore } from 'pinia';
import { useAuthStore } from './auth.store';
import { useLoadingStore } from './loading.store';

// verified: the account is active; expired: the link is older than 1 hour; unknown: wrong or already used link
export type EmailVerification = 'verified' | 'expired' | 'unknown' | 'error';

// The verification of the email of the customer, with the link (valid for 1 hour) they receive by email
export const useEmailVerificationStore = defineStore('emailVerification', () => {
  const apiUrl = import.meta.env.VITE_API_URL;

  // The link can be opened without being signed in
  async function verifyEmail(token: string): Promise<EmailVerification> {
    try {
      const response = await useLoadingStore().track(
        fetch(`${apiUrl}/api/accounts/email-verification`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ token }),
        })
      );
      if (response.ok) {
        useAuthStore().markEmailVerified();
        return 'verified';
      }
      if (response.status === 410) return 'expired';
      if (response.status === 404) return 'unknown';
      return 'error';
    } catch (error) {
      console.error('Email verification error:', error);
      return 'error';
    }
  }

  // A new link for the signed-in customer; the previous ones stop working. Returns whether it was sent
  async function sendVerificationEmail(): Promise<boolean> {
    try {
      const response = await useAuthStore().authFetch('/api/accounts/me/verification-email', { method: 'POST' });
      return response.ok;
    } catch (error) {
      console.error('Verification email error:', error);
      return false;
    }
  }

  return { verifyEmail, sendVerificationEmail };
});
