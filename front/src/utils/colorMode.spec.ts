import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { forgetColorMode, initialColorMode, oppositeColorMode, rememberColorMode } from './colorMode';
import { rememberCookieChoice } from './cookieConsent';

function systemPrefersDark(prefersDark: boolean) {
  vi.stubGlobal('matchMedia', (query: string) => ({ matches: prefersDark, media: query }));
}

describe('colorMode', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it.each([
    { prefersDark: true, expected: 'dark' },
    { prefersDark: false, expected: 'light' },
  ])(
    'without a choice, a system that prefers dark: $prefersDark should give the $expected mode',
    ({ prefersDark, expected }) => {
      // Arrange
      systemPrefersDark(prefersDark);
      // Act
      const colorMode = initialColorMode();
      // Assert
      expect(colorMode).toBe(expected);
    }
  );

  it('the mode chosen by the customer should win over the one of the system', () => {
    // Arrange
    systemPrefersDark(true);
    rememberCookieChoice('accepted');
    rememberColorMode('light');
    // Act
    const colorMode = initialColorMode();
    // Assert
    expect(colorMode).toBe('light');
  });

  it.each(['undecided', 'refused'] as const)(
    'the mode should not be remembered when the cookie choice is "%s"',
    (choice) => {
      // Arrange
      rememberCookieChoice(choice);
      // Act
      rememberColorMode('dark');
      // Assert
      expect(localStorage.getItem('flexstore.colorMode')).toBeNull();
    }
  );

  it('a forgotten mode should no longer be stored', () => {
    // Arrange
    rememberCookieChoice('accepted');
    rememberColorMode('dark');
    // Act
    forgetColorMode();
    // Assert
    expect(localStorage.getItem('flexstore.colorMode')).toBeNull();
  });

  it('an unknown stored mode should be ignored', () => {
    // Arrange
    systemPrefersDark(true);
    localStorage.setItem('flexstore.colorMode', 'sepia');
    // Act
    const colorMode = initialColorMode();
    // Assert
    expect(colorMode).toBe('dark');
  });

  it.each([
    { colorMode: 'light', expected: 'dark' },
    { colorMode: 'dark', expected: 'light' },
  ] as const)('the opposite of $colorMode should be $expected', ({ colorMode, expected }) => {
    // Act
    const opposite = oppositeColorMode(colorMode);
    // Assert
    expect(opposite).toBe(expected);
  });
});
