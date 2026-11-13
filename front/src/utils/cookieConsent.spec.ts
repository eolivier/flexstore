import { beforeEach, describe, expect, it } from 'vitest';
import { preferencesAccepted, rememberCookieChoice, storedCookieChoice } from './cookieConsent';

describe('cookieConsent', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should be undecided before the customer chose', () => {
    // Act
    const choice = storedCookieChoice();
    // Assert
    expect(choice).toBe('undecided');
  });

  it.each(['accepted', 'refused'] as const)('should remember the choice "%s"', (choice) => {
    // Arrange
    rememberCookieChoice(choice);
    // Act
    const storedChoice = storedCookieChoice();
    // Assert
    expect(storedChoice).toBe(choice);
  });

  it('should be undecided when the stored value is unknown', () => {
    // Arrange
    localStorage.setItem('flexstore.cookieChoice', 'maybe');
    // Act
    const choice = storedCookieChoice();
    // Assert
    expect(choice).toBe('undecided');
  });

  it.each([
    { choice: 'accepted', allowed: true },
    { choice: 'refused', allowed: false },
    { choice: 'undecided', allowed: false },
  ] as const)('with the choice "$choice", the preferences should be accepted: $allowed', ({ choice, allowed }) => {
    // Arrange
    rememberCookieChoice(choice);
    // Act
    const accepted = preferencesAccepted();
    // Assert
    expect(accepted).toBe(allowed);
  });
});
