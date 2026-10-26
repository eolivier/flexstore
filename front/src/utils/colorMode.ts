// Light or dark: the mode chosen by the customer, kept in the browser; otherwise the one of their system
export type ColorMode = 'light' | 'dark';

const COLOR_MODE_KEY = 'flexstore.colorMode';

export const themeNames: Record<ColorMode, string> = { light: 'flexstoreLight', dark: 'flexstoreDark' };

function isColorMode(value: string | null): value is ColorMode {
  return value === 'light' || value === 'dark';
}

function systemColorMode(): ColorMode {
  const prefersDark = globalThis.matchMedia?.('(prefers-color-scheme: dark)').matches;
  return prefersDark ? 'dark' : 'light';
}

// The storage may be unavailable (private browsing, blocked site data): the system mode applies then
export function initialColorMode(): ColorMode {
  try {
    const storedMode = localStorage.getItem(COLOR_MODE_KEY);
    return isColorMode(storedMode) ? storedMode : systemColorMode();
  } catch {
    return systemColorMode();
  }
}

export function rememberColorMode(colorMode: ColorMode): void {
  try {
    localStorage.setItem(COLOR_MODE_KEY, colorMode);
  } catch {
    // Not remembered: the next visit follows the system again
  }
}

export function oppositeColorMode(colorMode: ColorMode): ColorMode {
  return colorMode === 'dark' ? 'light' : 'dark';
}
