import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AbandonedCartReminderSetting from './AbandonedCartReminderSetting.vue';
import { useSettingsStore } from '@/stores/settings.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function mountSetting(activation: 'ENABLED' | 'DISABLED') {
  useSettingsStore().settings = {
    orderConfirmationEmail: 'ENABLED',
    paymentDelayMinutes: 10,
    freeShippingThreshold: 50,
    sellerSetup: 'ENABLED',
    abandonedCartReminder: activation,
    abandonedCartDelayHours: 24,
  };
  return mount(AbandonedCartReminderSetting, { global: { plugins: [vuetify, i18n] } });
}

describe('AbandonedCartReminderSetting.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it.each([
    { activation: 'ENABLED' as const, checked: true },
    { activation: 'DISABLED' as const, checked: false },
  ])('the switch is on when the reminder is $activation: $checked', ({ activation, checked }) => {
    // Act
    const wrapper = mountSetting(activation);
    // Assert
    expect((wrapper.find('input[type="checkbox"]').element as HTMLInputElement).checked).toBe(checked);
  });

  it.each([
    { activation: 'ENABLED' as const, wished: false, path: 'disable' },
    { activation: 'DISABLED' as const, wished: true, path: 'enable' },
  ])('turning the switch of an $activation reminder calls /$path', async ({ activation, wished, path }) => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve({}) });
    const wrapper = mountSetting(activation);
    // Act
    await wrapper.find('input[type="checkbox"]').setValue(wished);
    await flushPromises();
    // Assert
    expect(mockFetch.mock.lastCall![0]).toBe(
      `http://localhost:8080/api/admin/settings/abandoned-cart-reminder/${path}`
    );
  });

  it('shows the stored delay', () => {
    // Act
    const wrapper = mountSetting('ENABLED');
    // Assert
    expect((wrapper.find('input[type="number"]').element as HTMLInputElement).value).toBe('24');
  });

  it('saves the new delay', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve({}) });
    const wrapper = mountSetting('ENABLED');
    await wrapper.find('input[type="number"]').setValue('48');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/settings/abandoned-cart-delay');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ hours: 48 });
    expect(wrapper.text()).toContain('Saved.');
  });

  it.each(['0', '721'])('refuses a delay of %s hours', async (hours) => {
    // Arrange
    const wrapper = mountSetting('ENABLED');
    // Act
    await wrapper.find('input[type="number"]').setValue(hours);
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('From 1 to 720 hours (30 days)');
  });
});
