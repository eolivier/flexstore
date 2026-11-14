import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { useGuidedTourStore } from './guidedTour.store';

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

async function storeSignedInWith(guidedTour: string) {
  signedIn();
  answer(200, { guidedTour });
  const store = useGuidedTourStore();
  await flushPromises();
  return store;
}

describe('guidedTour.store', () => {
  beforeEach(() => {
    sessionStorage.clear();
    mockFetch.mockReset();
    setActivePinia(createPinia());
  });

  it('should not open for a visitor', async () => {
    // Act
    const store = useGuidedTourStore();
    await flushPromises();
    // Assert
    expect(store.isOpen).toBe(false);
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it.each(['NONE', 'SHOWN'])('should open at sign-in when the account holds %s', async (guidedTour) => {
    // Act
    const store = await storeSignedInWith(guidedTour);
    // Assert
    expect(store.isOpen).toBe(true);
  });

  it('should not open at sign-in when the customer hid it', async () => {
    // Act
    const store = await storeSignedInWith('HIDDEN');
    // Assert
    expect(store.isOpen).toBe(false);
  });

  it('should not open when the account cannot be read', async () => {
    // Arrange
    signedIn();
    answer(500);
    // Act
    const store = useGuidedTourStore();
    await flushPromises();
    // Assert
    expect(store.isOpen).toBe(false);
  });

  it('should close without recording anything when skipped', async () => {
    // Arrange
    const store = await storeSignedInWith('NONE');
    // Act
    store.skip();
    // Assert
    expect(store.isOpen).toBe(false);
    expect(mockFetch).toHaveBeenCalledTimes(1);
  });

  it('should close the menu it presented when skipped', async () => {
    // Arrange
    const store = await storeSignedInWith('NONE');
    store.presentMenu('account');
    // Act
    store.skip();
    // Assert
    expect(store.presentedMenu).toBe('');
  });

  it('should celebrate the end of the tour, then stop', async () => {
    // Arrange
    vi.useFakeTimers();
    const store = await storeSignedInWith('NONE');
    // Act
    await store.finish(false);
    // Assert
    expect(store.isCelebrating).toBe(true);
    vi.advanceTimersByTime(5000);
    expect(store.isCelebrating).toBe(false);
    vi.useRealTimers();
  });

  it('should not celebrate when skipped', async () => {
    // Arrange
    const store = await storeSignedInWith('NONE');
    // Act
    store.skip();
    // Assert
    expect(store.isCelebrating).toBe(false);
  });

  it('should hide the tour in the account when finished with "Do not show again"', async () => {
    // Arrange
    const store = await storeSignedInWith('NONE');
    answer(200, { guidedTour: 'HIDDEN' });
    // Act
    await store.finish(true);
    // Assert
    expect(store.isOpen).toBe(false);
    expect(store.isShown).toBe(false);
    const [url, init] = mockFetch.mock.calls[1];
    expect(String(url)).toContain('/api/guided-tour');
    expect(init).toMatchObject({ method: 'PUT', body: JSON.stringify({ guidedTour: 'HIDDEN' }) });
  });

  it('should record nothing when finished without "Do not show again"', async () => {
    // Arrange
    const store = await storeSignedInWith('NONE');
    // Act
    await store.finish(false);
    // Assert
    expect(store.isOpen).toBe(false);
    expect(store.isShown).toBe(true);
    expect(mockFetch).toHaveBeenCalledTimes(1);
  });

  it('should start the tour again when the customer turns it on in their preferences', async () => {
    // Arrange
    const store = await storeSignedInWith('HIDDEN');
    answer(200, { guidedTour: 'SHOWN' });
    // Act
    await store.choose(true);
    // Assert
    expect(store.isShown).toBe(true);
    expect(store.isOpen).toBe(true);
  });

  it('should not open the tour when the customer turns it off in their preferences', async () => {
    // Arrange
    const store = await storeSignedInWith('SHOWN');
    answer(200, { guidedTour: 'HIDDEN' });
    // Act
    await store.choose(false);
    // Assert
    expect(store.isShown).toBe(false);
    expect(store.isOpen).toBe(false);
  });
});
