import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { SELLER_SETUP_PATH, sellerSetupIsPendingFor } from './sellerSetupRedirect';
import { useSettingsStore, type ShopSettings } from '@/stores/settings.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const settings: ShopSettings = {
  orderConfirmationEmail: 'ENABLED',
  paymentDelayMinutes: 10,
  freeShippingThreshold: 50,
  sellerSetup: 'DISABLED',
  abandonedCartReminder: 'DISABLED',
  abandonedCartDelayHours: 24,
};

describe('sellerSetupIsPendingFor', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should be pending for a page of the administration while the seller setup is off', async () => {
    // Arrange
    useSettingsStore().settings = settings;
    // Act
    const pending = await sellerSetupIsPendingFor('/admin/products');
    // Assert
    expect(pending).toBe(true);
  });

  it('should not be pending once the seller setup is on', async () => {
    // Arrange
    useSettingsStore().settings = { ...settings, sellerSetup: 'ENABLED' };
    // Act
    const pending = await sellerSetupIsPendingFor('/admin/products');
    // Assert
    expect(pending).toBe(false);
  });

  it('should not be pending for the assistant itself', async () => {
    // Arrange
    useSettingsStore().settings = settings;
    // Act
    const pending = await sellerSetupIsPendingFor(SELLER_SETUP_PATH);
    // Assert
    expect(pending).toBe(false);
  });

  it('should read the settings when they are not loaded yet', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: async () => settings });
    // Act
    const pending = await sellerSetupIsPendingFor('/admin/orders');
    // Assert
    expect(pending).toBe(true);
  });

  it('should keep the administration open when the settings cannot be read', async () => {
    // Arrange
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    // Act
    const pending = await sellerSetupIsPendingFor('/admin/orders');
    // Assert
    expect(pending).toBe(false);
  });
});
