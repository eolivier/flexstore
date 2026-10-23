import { describe, expect, it } from 'vitest';
import { formatPrice } from './price';

describe('formatPrice', () => {
  it('formats with 2 decimals in French followed by the currency', () => {
    expect(formatPrice(1249.5, '€', 'fr')).toBe('1 249,50 €');
  });

  it('formats with 2 decimals in English followed by the currency', () => {
    expect(formatPrice(25, '€', 'en')).toBe('25.00 €');
  });

  it('formats a missing amount as zero without currency', () => {
    expect(formatPrice(undefined, undefined, 'en')).toBe('0.00');
  });
});
