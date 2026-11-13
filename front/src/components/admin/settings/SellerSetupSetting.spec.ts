import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import SellerSetupSetting from './SellerSetupSetting.vue';
import { useSettingsStore } from '@/stores/settings.store';
import { vuetify, i18n } from '@/test/setup';

function mountSetting(sellerSetup: 'ENABLED' | 'DISABLED') {
  useSettingsStore().settings = {
    orderConfirmationEmail: 'ENABLED',
    paymentDelayMinutes: 10,
    freeShippingThreshold: 50,
    sellerSetup,
    abandonedCartReminder: 'DISABLED',
    abandonedCartDelayHours: 24,
  };
  const stubs = { 'router-link': { template: '<a class="setup-link"><slot /></a>' } };
  return mount(SellerSetupSetting, { global: { plugins: [vuetify, i18n], stubs } });
}

const switchOf = (wrapper: ReturnType<typeof mountSetting>) =>
  wrapper.find('input[type="checkbox"]').element as HTMLInputElement;

describe('SellerSetupSetting.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it.each([
    { sellerSetup: 'ENABLED' as const, checked: true },
    { sellerSetup: 'DISABLED' as const, checked: false },
  ])('the switch should be $checked when the seller setup is $sellerSetup', ({ sellerSetup, checked }) => {
    // Act
    const wrapper = mountSetting(sellerSetup);
    // Assert
    expect(switchOf(wrapper).checked).toBe(checked);
  });

  it.each(['ENABLED' as const, 'DISABLED' as const])('the switch should not be changed when it is %s', (sellerSetup) => {
    // Act
    const wrapper = mountSetting(sellerSetup);
    // Assert
    expect(switchOf(wrapper).disabled).toBe(true);
  });

  it('should offer the assistant while the seller setup is off', () => {
    // Act
    const wrapper = mountSetting('DISABLED');
    // Assert
    expect(wrapper.find('.setup-link').exists()).toBe(true);
  });

  it('should not offer the assistant once the seller setup is on', () => {
    // Act
    const wrapper = mountSetting('ENABLED');
    // Assert
    expect(wrapper.find('.setup-link').exists()).toBe(false);
  });
});
