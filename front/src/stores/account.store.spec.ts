import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAccountStore } from './account.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('account.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  it('should read the account of the signed-in customer', async () => {
    // Arrange
    const account = { phoneNumber: '+33612345678', addresses: [] };
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(account) });
    const store = useAccountStore();
    // Act
    const readAccount = await store.fetchAccount();
    // Assert
    expect(readAccount).toEqual(account);
    expect(mockFetch.mock.calls[0][0]).toContain('/api/accounts/me');
  });

  it('should read no account when the API refuses', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 401 });
    const store = useAccountStore();
    // Act
    const readAccount = await store.fetchAccount();
    // Assert
    expect(readAccount).toBeNull();
  });

  it('should read no account and log the failure when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValue(new Error('offline'));
    const store = useAccountStore();
    // Act
    const readAccount = await store.fetchAccount();
    // Assert
    expect(readAccount).toBeNull();
    expect(console.error).toHaveBeenCalledWith('Failed to read the account', expect.any(Error));
  });
});
