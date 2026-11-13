import { beforeEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import CookieBanner from './CookieBanner.vue';
import { rememberCookieChoice, storedCookieChoice } from '@/utils/cookieConsent';

const mountBanner = () => mount(CookieBanner);
const buttonLabelled = (wrapper: ReturnType<typeof mountBanner>, label: string) =>
  wrapper.findAll('button').find((button) => button.text() === label)!;

describe('CookieBanner.vue', () => {
  beforeEach(() => {
    localStorage.clear();
    setActivePinia(createPinia());
  });

  it('should ask the customer who has not chosen yet', () => {
    // Act
    const wrapper = mountBanner();
    // Assert
    expect(wrapper.text()).toContain('Cookies');
    expect(buttonLabelled(wrapper, 'Accept')).toBeDefined();
    expect(buttonLabelled(wrapper, 'Refuse')).toBeDefined();
  });

  it.each(['accepted', 'refused'] as const)('should not ask again a customer who %s', (choice) => {
    // Arrange
    rememberCookieChoice(choice);
    // Act
    const wrapper = mountBanner();
    // Assert
    expect(wrapper.find('button').exists()).toBe(false);
  });

  it.each([
    { label: 'Accept', choice: 'accepted' },
    { label: 'Refuse', choice: 'refused' },
  ])('the button "$label" should remember the choice "$choice" and close the banner', async ({ label, choice }) => {
    // Arrange
    const wrapper = mountBanner();
    // Act
    await buttonLabelled(wrapper, label).trigger('click');
    // Assert
    expect(storedCookieChoice()).toBe(choice);
    expect(wrapper.find('button').exists()).toBe(false);
  });
});
