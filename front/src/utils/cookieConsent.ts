// The cookie choice of the customer: the essential cookies (sign-in, cart) need no consent, the preference
// cookies (the color mode) are only kept once the customer accepted them.
// A visitor keeps their choice in the browser; a signed-in customer has it in their account (6 months), and the
// choice of the browser is then reported to the account and removed from the browser
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

// The choice in force: the one of the browser, or the one of the account once signed in
let appliedChoice: CookieChoice = storedCookieChoice();

export function applyCookieChoice(choice: CookieChoice): void {
  appliedChoice = choice;
}

// The choice of a visitor, kept in the browser
export function rememberCookieChoice(choice: CookieChoice): void {
  applyCookieChoice(choice);
  try {
    localStorage.setItem(COOKIE_CHOICE_KEY, choice);
  } catch {
    // Not remembered: the customer is asked again at the next visit
  }
}

// Once the choice is in the account of the customer, the browser keeps nothing
export function clearStoredCookieChoice(): void {
  try {
    localStorage.removeItem(COOKIE_CHOICE_KEY);
  } catch {
    // Nothing to clear
  }
}

export function preferencesAccepted(): boolean {
  return appliedChoice === 'accepted';
}
