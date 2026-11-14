import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { usePasswordResetStore } from './passwordReset.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('passwordReset.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  it('should send the trimmed email to request a reset', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 202 });
    const store = usePasswordResetStore();
    // Act
    const received = await store.requestReset('  jane@example.com ');
    // Assert
    expect(received).toBe(true);
    expect(mockFetch.mock.calls[0][0]).toBe('http://localhost:8080/api/password-reset-requests');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ email: 'jane@example.com' });
  });

  it('should report a request that the shop did not receive', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    const store = usePasswordResetStore();
    // Act
    const received = await store.requestReset('jane@example.com');
    // Assert
    expect(received).toBe(false);
  });

  it('should report a request that failed when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValue(new Error('offline'));
    const store = usePasswordResetStore();
    // Act
    const received = await store.requestReset('jane@example.com');
    // Assert
    expect(received).toBe(false);
    expect(console.error).toHaveBeenCalledWith('Failed to request the password reset', expect.any(Error));
  });

  it('should send the token and the new password to reset the password', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
    const store = usePasswordResetStore();
    // Act
    const reset = await store.resetPassword('token-1', 'new-password');
    // Assert
    expect(reset).toBe('reset');
    expect(mockFetch.mock.calls[0][0]).toBe('http://localhost:8080/api/password-resets');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ token: 'token-1', password: 'new-password' });
  });

  it.each([
    { status: 410, expected: 'expired' },
    { status: 404, expected: 'unknown' },
    { status: 422, expected: 'tooShort' },
    { status: 500, expected: 'error' },
  ])('a reset answered by $status should be $expected', async ({ status, expected }) => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status });
    const store = usePasswordResetStore();
    // Act
    const reset = await store.resetPassword('token-1', 'new-password');
    // Assert
    expect(reset).toBe(expected);
  });

  it('should report an error when the API cannot be reached to reset the password', async () => {
    // Arrange
    mockFetch.mockRejectedValue(new Error('offline'));
    const store = usePasswordResetStore();
    // Act
    const reset = await store.resetPassword('token-1', 'new-password');
    // Assert
    expect(reset).toBe('error');
    expect(console.error).toHaveBeenCalledWith('Failed to reset the password', expect.any(Error));
  });
});
