import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import OrderConfirmationEmailSetting from './OrderConfirmationEmailSetting.vue';
import { useSettingsStore } from '@/stores/settings.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function mountSetting(activation: 'ENABLED' | 'DISABLED') {
  const store = useSettingsStore();
  store.settings = { orderConfirmationEmail: activation };
  return mount(OrderConfirmationEmailSetting, { global: { plugins: [vuetify, i18n] } });
}

describe('OrderConfirmationEmailSetting.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it.each([
    { activation: 'ENABLED' as const, checked: true },
    { activation: 'DISABLED' as const, checked: false },
  ])('the switch is on when the email is $activation: $checked', ({ activation, checked }) => {
    // Act
    const wrapper = mountSetting(activation);
    // Assert
    expect((wrapper.find('input[type="checkbox"]').element as HTMLInputElement).checked).toBe(checked);
  });

  it.each([
    { activation: 'ENABLED' as const, wished: false, path: 'disable' },
    { activation: 'DISABLED' as const, wished: true, path: 'enable' },
  ])('turning the switch of an $activation email calls /$path', async ({ activation, wished, path }) => {
    // Arrange
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ orderConfirmationEmail: activation }),
    });
    const wrapper = mountSetting(activation);
    // Act
    await wrapper.find('input[type="checkbox"]').setValue(wished);
    await flushPromises();
    // Assert
    expect(mockFetch.mock.lastCall![0]).toBe(
      `http://localhost:8080/api/admin/settings/order-confirmation-email/${path}`
    );
  });
});
