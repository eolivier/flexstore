import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { useColorModeStore } from './colorMode.store';
import { rememberColorMode } from '@/utils/colorMode';
import { rememberCookieChoice } from '@/utils/cookieConsent';

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

const COLOR_MODE_KEY = 'flexstore.colorMode';

describe('colorMode.store, signed out', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    mockFetch.mockReset();
    setActivePinia(createPinia());
  });

  it('should remember the chosen mode in the browser once the preference cookies are accepted', async () => {
    // Arrange
    rememberCookieChoice('accepted');
    const store = useColorModeStore();
    // Act
    await store.choose('dark');
    // Assert
    expect(localStorage.getItem(COLOR_MODE_KEY)).toBe('dark');
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it('should not remember the chosen mode in the browser without the preference cookies', async () => {
    // Arrange
    rememberCookieChoice('refused');
    const store = useColorModeStore();
    // Act
    await store.choose('dark');
    // Assert
    expect(localStorage.getItem(COLOR_MODE_KEY)).toBeNull();
  });
});

describe('colorMode.store, signed in', () => {
  beforeEach(() => {
    localStorage.clear();
    mockFetch.mockReset();
    signedIn();
    setActivePinia(createPinia());
  });

  it.each([
    { stored: 'LIGHT', expected: 'light' },
    { stored: 'DARK', expected: 'dark' },
    { stored: 'NONE', expected: 'none' },
  ])('the account holding $stored gives the mode $expected', async ({ stored, expected }) => {
    // Arrange
    answer(200, { colorMode: stored });
    // Act
    const store = useColorModeStore();
    await flushPromises();
    // Assert
    expect(mockFetch.mock.lastCall![0]).toBe('http://localhost:8080/api/color-mode');
    expect(store.accountColorMode).toBe(expected);
  });

  it('should report the mode chosen in the browser to an account that has none', async () => {
    // Arrange
    rememberCookieChoice('accepted');
    rememberColorMode('dark');
    answer(200, { colorMode: 'NONE' });
    answer(200, { colorMode: 'DARK' });
    // Act
    useColorModeStore();
    await flushPromises();
    // Assert
    const [, init] = mockFetch.mock.lastCall!;
    expect(init.method).toBe('PUT');
    expect(init.body).toBe(JSON.stringify({ colorMode: 'DARK' }));
  });

  it('should not override the mode of the account with the one of the browser', async () => {
    // Arrange
    rememberCookieChoice('accepted');
    rememberColorMode('dark');
    answer(200, { colorMode: 'LIGHT' });
    // Act
    const store = useColorModeStore();
    await flushPromises();
    // Assert
    expect(mockFetch).toHaveBeenCalledTimes(1);
    expect(store.accountColorMode).toBe('light');
  });

  it('should record a new choice in the account and keep nothing in the browser', async () => {
    // Arrange
    rememberCookieChoice('accepted');
    answer(200, { colorMode: 'NONE' });
    const store = useColorModeStore();
    await flushPromises();
    answer(200, { colorMode: 'DARK' });
    // Act
    await store.choose('dark');
    // Assert
    const [, init] = mockFetch.mock.lastCall!;
    expect(init.method).toBe('PUT');
    expect(init.body).toBe(JSON.stringify({ colorMode: 'DARK' }));
    expect(localStorage.getItem(COLOR_MODE_KEY)).toBeNull();
  });

  it('should keep the choice in the browser when the account could not record it', async () => {
    // Arrange
    rememberCookieChoice('accepted');
    answer(200, { colorMode: 'NONE' });
    const store = useColorModeStore();
    await flushPromises();
    answer(500);
    // Act
    await store.choose('dark');
    // Assert
    expect(localStorage.getItem(COLOR_MODE_KEY)).toBe('dark');
  });
});
