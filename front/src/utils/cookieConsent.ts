// The cookie choice of the customer, kept in the browser: the essential cookies (sign-in, cart) need no consent,
// the preference cookies (the color mode) are only kept once the customer accepted them
export type CookieChoice = 'undecided' | 'accepted' | 'refused';

const COOKIE_CHOICE_KEY = 'flexstore.cookieChoice';

function isDecided(value: string | null): value is 'accepted' | 'refused' {
  return value === 'accepted' || value === 'refused';
}

// The storage may be unavailable (private browsing, blocked site data): the customer is asked again then
export function storedCookieChoice(): CookieChoice {
  try {
    const storedChoice = localStorage.getItem(COOKIE_CHOICE_KEY);
    return isDecided(storedChoice) ? storedChoice : 'undecided';
  } catch {
    return 'undecided';
  }
}

export function rememberCookieChoice(choice: CookieChoice): void {
  try {
    localStorage.setItem(COOKIE_CHOICE_KEY, choice);
  } catch {
    // Not remembered: the customer is asked again at the next visit
  }
}

export function preferencesAccepted(): boolean {
  return storedCookieChoice() === 'accepted';
}
