import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useCookieConsentStore } from './cookieConsent.store';
import { rememberColorMode } from '@/utils/colorMode';
import { rememberCookieChoice, storedCookieChoice } from '@/utils/cookieConsent';

describe('cookieConsent.store', () => {
  beforeEach(() => {
    localStorage.clear();
    setActivePinia(createPinia());
  });

  it('should be undecided at the first visit', () => {
    // Act
    const store = useCookieConsentStore();
    // Assert
    expect(store.isUndecided).toBe(true);
    expect(store.preferencesAccepted).toBe(false);
  });

  it('should start with the choice remembered at a previous visit', () => {
    // Arrange
    rememberCookieChoice('accepted');
    // Act
    const store = useCookieConsentStore();
    // Assert
    expect(store.isUndecided).toBe(false);
    expect(store.preferencesAccepted).toBe(true);
  });

  it('should remember an accepted choice', () => {
    // Arrange
    const store = useCookieConsentStore();
    // Act
    store.choose('accepted');
    // Assert
    expect(store.preferencesAccepted).toBe(true);
    expect(storedCookieChoice()).toBe('accepted');
  });

  it('should forget the color mode when the preferences are refused', () => {
    // Arrange
    const store = useCookieConsentStore();
    store.choose('accepted');
    rememberColorMode('dark');
    // Act
    store.choose('refused');
    // Assert
    expect(store.preferencesAccepted).toBe(false);
    expect(localStorage.getItem('flexstore.colorMode')).toBeNull();
  });
});
