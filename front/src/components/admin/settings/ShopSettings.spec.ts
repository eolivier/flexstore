import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ShopSettings from './ShopSettings.vue';
import { useSettingsStore, type ShopSettings as Settings } from '@/stores/settings.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const settings: Settings = {
  orderConfirmationEmail: 'ENABLED',
  paymentDelayMinutes: 10,
  freeShippingThreshold: 50,
  sellerSetup: 'ENABLED',
  abandonedCartReminder: 'DISABLED',
  abandonedCartDelayHours: 24,
};

function mountSettings() {
  const stubs = { AdminBreadcrumbs: true, RouterLink: true };
  return mount(ShopSettings, { global: { plugins: [vuetify, i18n], stubs } });
}

describe('ShopSettings.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should show the settings once they are read', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(settings) });
    // Act
    const wrapper = mountSettings();
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('Free shipping');
    expect(useSettingsStore().load).toEqual({ status: 'loaded', settings });
  });

  it('should tell that the settings could not be read', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: false, status: 500, json: () => Promise.resolve({}) });
    // Act
    const wrapper = mountSettings();
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('The settings could not be loaded.');
  });

  it('should wait for the settings without showing them', () => {
    // Arrange
    mockFetch.mockReturnValueOnce(new Promise(() => {}));
    // Act
    const wrapper = mountSettings();
    // Assert
    expect(wrapper.find('.v-skeleton-loader').exists()).toBe(true);
    expect(wrapper.text()).not.toContain('Free shipping');
  });
});
