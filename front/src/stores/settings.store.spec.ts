import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useSettingsStore } from './settings.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const answer = (status: number, body?: unknown) =>
  mockFetch.mockResolvedValueOnce({ ok: status < 300, status, json: () => Promise.resolve(body) });

describe('settings.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should load the settings', async () => {
    // Arrange
    answer(200, { orderConfirmationEmail: 'ENABLED' });
    const store = useSettingsStore();
    // Act
    await store.fetchSettings();
    // Assert
    expect(store.load).toEqual({ status: 'loaded', settings: { orderConfirmationEmail: 'ENABLED' } });
    expect(mockFetch.mock.lastCall![0]).toBe('http://localhost:8080/api/admin/settings');
  });

  it('should be loading until the settings are read', () => {
    // Act
    const store = useSettingsStore();
    // Assert
    expect(store.load).toEqual({ status: 'loading' });
  });

  it('should fail when the settings cannot be read', async () => {
    // Arrange
    answer(500);
    const store = useSettingsStore();
    // Act
    await store.fetchSettings();
    // Assert
    expect(store.load).toEqual({ status: 'failed' });
  });

  it.each([
    { intent: 'disableOrderConfirmationEmail' as const, path: 'disable', activation: 'DISABLED' },
    { intent: 'enableOrderConfirmationEmail' as const, path: 'enable', activation: 'ENABLED' },
  ])('$intent should post to /$path and keep the saved settings', async ({ intent, path, activation }) => {
    // Arrange
    answer(200, { orderConfirmationEmail: activation });
    const store = useSettingsStore();
    // Act
    const saved = await store[intent]();
    // Assert
    expect(saved).toBe(true);
    expect(store.load).toEqual({ status: 'loaded', settings: { orderConfirmationEmail: activation } });
    const [url, init] = mockFetch.mock.lastCall!;
    expect(url).toBe(`http://localhost:8080/api/admin/settings/order-confirmation-email/${path}`);
    expect(init.method).toBe('POST');
  });

  it('should report a change that was not saved', async () => {
    // Arrange
    answer(500);
    const store = useSettingsStore();
    // Act
    const saved = await store.disableOrderConfirmationEmail();
    // Assert
    expect(saved).toBe(false);
  });
});
