import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { initialColorMode, oppositeColorMode, rememberColorMode } from './colorMode';

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
    'without a choice, a system that prefers dark: $prefersDark gives the $expected mode',
    ({ prefersDark, expected }) => {
      // Arrange
      systemPrefersDark(prefersDark);
      // Act
      const colorMode = initialColorMode();
      // Assert
      expect(colorMode).toBe(expected);
    }
  );

  it('the mode chosen by the customer wins over the one of the system', () => {
    // Arrange
    systemPrefersDark(true);
    rememberColorMode('light');
    // Act
    const colorMode = initialColorMode();
    // Assert
    expect(colorMode).toBe('light');
  });

  it('an unknown stored mode is ignored', () => {
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
  ] as const)('the opposite of $colorMode is $expected', ({ colorMode, expected }) => {
    // Act
    const opposite = oppositeColorMode(colorMode);
    // Assert
    expect(opposite).toBe(expected);
  });
});
