import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { useLanguageStore } from './language.store';

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

describe('language.store, signed out', () => {
  beforeEach(() => {
    sessionStorage.clear();
    mockFetch.mockReset();
    setActivePinia(createPinia());
  });

  it('should hold no language of an account and call nothing', async () => {
    // Act
    const store = useLanguageStore();
    await store.choose('fr');
    // Assert
    expect(store.accountLanguage).toBe('none');
    expect(mockFetch).not.toHaveBeenCalled();
  });
});

describe('language.store, signed in', () => {
  beforeEach(() => {
    mockFetch.mockReset();
    signedIn();
    setActivePinia(createPinia());
  });

  it.each([
    { stored: 'FR', expected: 'fr' },
    { stored: 'EN', expected: 'en' },
    { stored: 'NONE', expected: 'none' },
  ])('the account holding $stored should give the language $expected', async ({ stored, expected }) => {
    // Arrange
    answer(200, { language: stored });
    // Act
    const store = useLanguageStore();
    await flushPromises();
    // Assert
    expect(mockFetch.mock.lastCall![0]).toBe('http://localhost:8080/api/language');
    expect(store.accountLanguage).toBe(expected);
  });

  it('should hold no language when the account could not be read', async () => {
    // Arrange
    answer(500);
    // Act
    const store = useLanguageStore();
    await flushPromises();
    // Assert
    expect(store.accountLanguage).toBe('none');
  });

  it('should record a new choice in the account', async () => {
    // Arrange
    answer(200, { language: 'NONE' });
    const store = useLanguageStore();
    await flushPromises();
    answer(200, { language: 'FR' });
    // Act
    await store.choose('fr');
    // Assert
    const [url, init] = mockFetch.mock.lastCall!;
    expect(url).toBe('http://localhost:8080/api/language');
    expect(init.method).toBe('PUT');
    expect(init.body).toBe(JSON.stringify({ language: 'FR' }));
  });
});
