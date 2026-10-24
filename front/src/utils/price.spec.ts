import { describe, expect, it } from 'vitest';
import { formatPrice } from './price';

describe('formatPrice', () => {
  it.each([
    { amount: 1249.5, currency: '€', locale: 'fr', expected: '1 249,50 €' },
    { amount: 25, currency: '€', locale: 'en', expected: '25.00 €' },
    { amount: undefined, currency: undefined, locale: 'en', expected: '0.00' },
  ])('formats $amount $currency in $locale as "$expected"', ({ amount, currency, locale, expected }) => {
    // Act
    const formatted = formatPrice(amount, currency, locale);
    // Assert
    expect(formatted).toBe(expected);
  });
});
