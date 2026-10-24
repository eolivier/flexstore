import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useEmailVerificationStore } from './emailVerification.store';
import { useAuthStore } from './auth.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const pendingCustomer = { id: 'u-1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', role: 'USER', status: 'PENDING_VERIFICATION' };

async function signedInPendingCustomer() {
  mockFetch.mockResolvedValueOnce({
    ok: true,
    json: () => Promise.resolve({ token: 'jwt-token', expiresAt: '2030-01-01T00:00:00Z', user: pendingCustomer }),
  });
  const authStore = useAuthStore();
  await authStore.login('jane@example.com', 'password123');
  return authStore;
}

describe('emailVerification.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
  });

  it.each([
    { status: 204, expected: 'verified' },
    { status: 410, expected: 'expired' },
    { status: 404, expected: 'unknown' },
    { status: 500, expected: 'error' },
  ])('a verification answered $status is $expected', async ({ status, expected }) => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: status < 300, status });
    const store = useEmailVerificationStore();
    // Act
    const verification = await store.verifyEmail('the-token');
    // Assert
    expect(verification).toBe(expected);
  });

  it('should send the token of the link', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: true, status: 204 });
    const store = useEmailVerificationStore();
    // Act
    await store.verifyEmail('the-token');
    // Assert
    const [url, init] = mockFetch.mock.calls[0];
    expect(url).toMatch(/\/api\/accounts\/email-verification$/);
    expect(JSON.parse(init.body)).toEqual({ token: 'the-token' });
  });

  it('a verified email should activate the account of the signed-in customer, in the tab session too', async () => {
    // Arrange
    const authStore = await signedInPendingCustomer();
    mockFetch.mockResolvedValueOnce({ ok: true, status: 204 });
    // Act
    await useEmailVerificationStore().verifyEmail('the-token');
    // Assert
    expect(authStore.currentUser?.status).toBe('ACTIVE');
    expect(JSON.parse(sessionStorage.getItem('flexstore.session')!).user.status).toBe('ACTIVE');
  });

  it.each([
    { ok: true, expected: true },
    { ok: false, expected: false },
  ])('a new link answered ok=$ok is sent=$expected', async ({ ok, expected }) => {
    // Arrange
    await signedInPendingCustomer();
    mockFetch.mockResolvedValueOnce({ ok, status: ok ? 204 : 409 });
    // Act
    const sent = await useEmailVerificationStore().sendVerificationEmail();
    // Assert
    expect(sent).toBe(expected);
    const [url, init] = mockFetch.mock.calls[1];
    expect(url).toMatch(/\/api\/accounts\/me\/verification-email$/);
    expect(new Headers(init.headers).get('Authorization')).toBe('Bearer jwt-token');
  });
});
