import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import PaymentDelaySetting from './PaymentDelaySetting.vue';
import { useSettingsStore } from '@/stores/settings.store';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function mountSetting() {
  useSettingsStore().settings = { orderConfirmationEmail: 'ENABLED', paymentDelayMinutes: 10 };
  return mount(PaymentDelaySetting, { global: { plugins: [vuetify, i18n] } });
}

describe('PaymentDelaySetting.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('shows the stored payment delay', () => {
    // Act
    const wrapper = mountSetting();
    // Assert
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('10');
  });

  it('saves the new payment delay', async () => {
    // Arrange
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ orderConfirmationEmail: 'ENABLED', paymentDelayMinutes: 25 }),
    });
    const wrapper = mountSetting();
    await wrapper.find('input').setValue('25');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/settings/payment-delay');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ minutes: 25 });
    expect(wrapper.text()).toContain('Saved.');
  });

  it('refuses a delay of 0 minutes', async () => {
    // Arrange
    const wrapper = mountSetting();
    // Act
    await wrapper.find('input').setValue('0');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('From 1 to 1440 minutes (1 day)');
  });
});
