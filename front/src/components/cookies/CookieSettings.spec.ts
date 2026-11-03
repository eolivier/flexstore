import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import CookieSettings from './CookieSettings.vue';
import { rememberCookieChoice, storedCookieChoice } from '@/utils/cookieConsent';

const mountSettings = () => mount(CookieSettings);
const preferencesSwitch = (wrapper: ReturnType<typeof mountSettings>) =>
  wrapper.find('input[type="checkbox"]:not([disabled])');

describe('CookieSettings.vue', () => {
  beforeEach(() => {
    localStorage.clear();
    setActivePinia(createPinia());
  });

  it('keeps the essential cookies on, whatever the customer chose', () => {
    // Act
    const wrapper = mountSettings();
    // Assert
    const essentialSwitch = wrapper.find('input[type="checkbox"][disabled]');
    expect((essentialSwitch.element as HTMLInputElement).checked).toBe(true);
  });

  it.each([
    { choice: 'accepted', checked: true },
    { choice: 'refused', checked: false },
    { choice: 'undecided', checked: false },
  ] as const)('with the choice "$choice", the preferences are on: $checked', ({ choice, checked }) => {
    // Arrange
    rememberCookieChoice(choice);
    // Act
    const wrapper = mountSettings();
    // Assert
    expect((preferencesSwitch(wrapper).element as HTMLInputElement).checked).toBe(checked);
  });

  it('turning the preferences on remembers the acceptance', async () => {
    // Arrange
    rememberCookieChoice('refused');
    const wrapper = mountSettings();
    // Act
    await preferencesSwitch(wrapper).setValue(true);
    // Assert
    expect(storedCookieChoice()).toBe('accepted');
  });

  it('turning the preferences off remembers the refusal', async () => {
    // Arrange
    rememberCookieChoice('accepted');
    const wrapper = mountSettings();
    // Act
    await preferencesSwitch(wrapper).setValue(false);
    // Assert
    expect(storedCookieChoice()).toBe('refused');
  });
});
