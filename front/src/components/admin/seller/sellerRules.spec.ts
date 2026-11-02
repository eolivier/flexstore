import { describe, expect, it } from 'vitest';
import { isEmail, isPhone, isSiret, isVatNumber, isWebsite } from './sellerRules';

describe('sellerRules', () => {
  it.each(['12345678900012', '123 456 789 00012'])('the SIRET "%s" is accepted', (siret) => {
    // Act
    const accepted = isSiret(siret);
    // Assert
    expect(accepted).toBe(true);
  });

  it.each(['', '1234567890001', '123456789000123', '1234567890001A'])('the SIRET "%s" is refused', (siret) => {
    // Act
    const accepted = isSiret(siret);
    // Assert
    expect(accepted).toBe(false);
  });

  it.each(['FR12345678901', 'fr 12 345678901', 'DE123456789'])(
    'the VAT number "%s" is accepted',
    (vatNumber) => {
      // Act
      const accepted = isVatNumber(vatNumber);
      // Assert
      expect(accepted).toBe(true);
    }
  );

  it.each(['', '12345678901', 'FR1', 'FR12345678901234', 'FR-1234567'])(
    'the VAT number "%s" is refused',
    (vatNumber) => {
      // Act
      const accepted = isVatNumber(vatNumber);
      // Assert
      expect(accepted).toBe(false);
    }
  );

  it.each([
    { rule: isPhone, value: '01 23 45 67 89', expected: true },
    { rule: isPhone, value: '+33 (0)1 23 45 67 89', expected: true },
    { rule: isPhone, value: 'abc', expected: false },
    { rule: isPhone, value: '', expected: false },
    { rule: isEmail, value: 'contact@flexstore.fr', expected: true },
    { rule: isEmail, value: 'contact.flexstore.fr', expected: false },
    { rule: isWebsite, value: 'www.flexstore.fr', expected: true },
    { rule: isWebsite, value: 'https://www.flexstore.fr/boutique', expected: true },
    { rule: isWebsite, value: 'flexstore', expected: false },
    { rule: isWebsite, value: '', expected: false },
  ])('$rule.name("$value") is $expected', ({ rule, value, expected }) => {
    // Act
    const accepted = rule(value);
    // Assert
    expect(accepted).toBe(expected);
  });
});
