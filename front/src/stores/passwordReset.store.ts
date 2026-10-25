import { defineStore } from 'pinia';
import { useLoadingStore } from './loading.store';

// reset: the customer signs in with the new password; expired: the link is older than 1 hour; unknown: wrong or
// already used link; tooShort: less than 8 characters
export type PasswordReset = 'reset' | 'expired' | 'unknown' | 'tooShort' | 'error';

// A customer who forgot their password receives a link (valid for 1 hour) and chooses a new one, signed out
export const usePasswordResetStore = defineStore('passwordReset', () => {
  const apiUrl = import.meta.env.VITE_API_URL;

  function post(path: string, body: object) {
    const request = fetch(`${apiUrl}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    return useLoadingStore().track(request);
  }

  // The answer is the same whether the email has an account or not. Returns whether the request was received
  async function requestReset(email: string): Promise<boolean> {
    try {
      const response = await post('/api/password-reset-requests', { email: email.trim() });
      return response.ok;
    } catch (error) {
      console.error('Password reset request error:', error);
      return false;
    }
  }

  async function resetPassword(token: string, password: string): Promise<PasswordReset> {
    try {
      const response = await post('/api/password-resets', { token, password });
      if (response.ok) return 'reset';
      if (response.status === 410) return 'expired';
      if (response.status === 404) return 'unknown';
      if (response.status === 422) return 'tooShort';
      return 'error';
    } catch (error) {
      console.error('Password reset error:', error);
      return 'error';
    }
  }

  return { requestReset, resetPassword };
});
