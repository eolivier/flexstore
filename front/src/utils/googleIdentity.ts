// The "Sign in with Google" button: the script of Google Identity Services is loaded when the button is first
// needed, and gives an ID token that the back end verifies (see POST /api/users/login/google)
const GOOGLE_IDENTITY_SCRIPT = 'https://accounts.google.com/gsi/client';

export interface GoogleCredentialResponse {
  credential: string;
}

interface GoogleIdentityServices {
  accounts: {
    id: {
      initialize(configuration: {
        client_id: string;
        callback: (response: GoogleCredentialResponse) => void;
      }): void;
      renderButton(parent: HTMLElement, options: Record<string, string | number>): void;
    };
  };
}

declare global {
  interface Window {
    google?: GoogleIdentityServices;
  }
}

// The OAuth client id of the shop; the button is not offered without it
export function googleClientId(): string {
  return import.meta.env.VITE_GOOGLE_CLIENT_ID ?? '';
}

export function googleSignInIsAvailable(): boolean {
  return googleClientId().trim() !== '';
}

let loadingScript: Promise<GoogleIdentityServices> | null = null;

export function loadGoogleIdentity(): Promise<GoogleIdentityServices> {
  if (window.google) return Promise.resolve(window.google);
  if (!loadingScript) {
    loadingScript = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = GOOGLE_IDENTITY_SCRIPT;
      script.async = true;
      script.onload = () =>
        window.google ? resolve(window.google) : reject(new Error('Google script is empty'));
      script.onerror = () => {
        // A new attempt is possible, e.g. once an ad blocker is turned off
        loadingScript = null;
        reject(new Error('Google script could not be loaded'));
      };
      document.head.appendChild(script);
    });
  }
  return loadingScript;
}
