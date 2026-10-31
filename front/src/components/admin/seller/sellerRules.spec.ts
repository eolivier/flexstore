import { describe, expect, it } from 'vitest';
import { isSiret, isVatNumber } from './sellerRules';

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
});
