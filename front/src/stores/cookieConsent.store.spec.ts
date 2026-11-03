import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { useCookieConsentStore } from './cookieConsent.store';
import { rememberColorMode } from '@/utils/colorMode';
import { rememberCookieChoice, storedCookieChoice } from '@/utils/cookieConsent';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const answer = (status: number, body?: unknown) =>
  mockFetch.mockResolvedValueOnce({ ok: status < 300, status, json: () => Promise.resolve(body) });

// The customer is signed in when the page loads: the session is restored from the tab
function signedIn() {
  const expiresAt = new Date(Date.now() + 3600_000).toISOString();
  const session = { token: 'token', expiresAt, user: { id: 'user-1', email: 'jane@flexstore.fr' } };
  sessionStorage.setItem('flexstore.session', JSON.stringify(session));
}

const COOKIE_CHOICE_KEY = 'flexstore.cookieChoice';

describe('cookieConsent.store, signed out', () => {
  beforeEach(() => {
    localStorage.clear();
    setActivePinia(createPinia());
  });

  it('should be undecided at the first visit', () => {
    // Act
    const store = useCookieConsentStore();
    // Assert
    expect(store.isUndecided).toBe(true);
    expect(store.preferencesAccepted).toBe(false);
  });

  it('should start with the choice remembered at a previous visit', () => {
    // Arrange
    rememberCookieChoice('accepted');
    // Act
    const store = useCookieConsentStore();
    // Assert
    expect(store.isUndecided).toBe(false);
    expect(store.preferencesAccepted).toBe(true);
  });

  it('should remember an accepted choice in the browser', async () => {
    // Arrange
    const store = useCookieConsentStore();
    // Act
    await store.choose('accepted');
    // Assert
    expect(store.preferencesAccepted).toBe(true);
    expect(storedCookieChoice()).toBe('accepted');
  });

  it('should forget the color mode when the preferences are refused', async () => {
    // Arrange
    const store = useCookieConsentStore();
    await store.choose('accepted');
    rememberColorMode('dark');
    // Act
    await store.choose('refused');
    // Assert
    expect(store.preferencesAccepted).toBe(false);
    expect(localStorage.getItem('flexstore.colorMode')).toBeNull();
  });
});

describe('cookieConsent.store, signed in', () => {
  beforeEach(() => {
    localStorage.clear();
    mockFetch.mockReset();
    signedIn();
    setActivePinia(createPinia());
  });

  it('should report the choice of the browser to the account and remove it from the browser', async () => {
    // Arrange
    rememberCookieChoice('accepted');
    answer(200, { choice: 'ACCEPTED', expiresAt: '2027-05-03T10:00:00Z' });
    // Act
    const store = useCookieConsentStore();
    await flushPromises();
    // Assert
    const [url, init] = mockFetch.mock.lastCall!;
    expect(url).toBe('http://localhost:8080/api/cookie-consent');
    expect(init.method).toBe('PUT');
    expect(init.body).toBe(JSON.stringify({ choice: 'ACCEPTED' }));
    expect(localStorage.getItem(COOKIE_CHOICE_KEY)).toBeNull();
    expect(store.preferencesAccepted).toBe(true);
  });

  it.each([
    { choice: 'ACCEPTED', accepted: true, undecided: false },
    { choice: 'REFUSED', accepted: false, undecided: false },
    { choice: 'UNDECIDED', accepted: false, undecided: true },
  ])('without a choice in the browser, the account says $choice', async ({ choice, accepted, undecided }) => {
    // Arrange
    answer(200, { choice });
    // Act
    const store = useCookieConsentStore();
    await flushPromises();
    // Assert
    expect(mockFetch.mock.lastCall![1]?.method).toBeUndefined();
    expect(store.preferencesAccepted).toBe(accepted);
    expect(store.isUndecided).toBe(undecided);
  });

  it('should not ask while the account is being read', () => {
    // Arrange
    answer(200, { choice: 'ACCEPTED' });
    // Act
    const store = useCookieConsentStore();
    // Assert
    expect(store.isUndecided).toBe(false);
  });

  it('should keep the choice of the browser when the account could not record it', async () => {
    // Arrange
    rememberCookieChoice('refused');
    answer(500);
    // Act
    useCookieConsentStore();
    await flushPromises();
    // Assert
    expect(storedCookieChoice()).toBe('refused');
  });

  it('should record a new choice in the account and keep nothing in the browser', async () => {
    // Arrange
    answer(200, { choice: 'UNDECIDED' });
    const store = useCookieConsentStore();
    await flushPromises();
    answer(200, { choice: 'REFUSED', expiresAt: '2027-05-03T10:00:00Z' });
    // Act
    await store.choose('refused');
    // Assert
    const [, init] = mockFetch.mock.lastCall!;
    expect(init.method).toBe('PUT');
    expect(init.body).toBe(JSON.stringify({ choice: 'REFUSED' }));
    expect(localStorage.getItem(COOKIE_CHOICE_KEY)).toBeNull();
    expect(store.isUndecided).toBe(false);
  });
});
