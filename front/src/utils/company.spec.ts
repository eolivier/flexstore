import { describe, expect, it } from 'vitest';
import { isValidSiren, isValidVatNumber } from './company';

describe('company', () => {
  it.each([
    { siren: '123456789', expected: true },
    { siren: '123 456 789', expected: true },
    { siren: '12345678', expected: false },
    { siren: '12345678A', expected: false },
    { siren: '', expected: false },
  ])('the SIREN "$siren" is valid: $expected', ({ siren, expected }) => {
    // Act
    const valid = isValidSiren(siren);
    // Assert
    expect(valid).toBe(expected);
  });

  it.each([
    { vatNumber: '', expected: true },
    { vatNumber: 'FR32123456789', expected: true },
    { vatNumber: 'fr 32 123 456 789', expected: true },
    { vatNumber: 'DE123456789', expected: true },
    { vatNumber: 'FR32987654321', expected: false },
    { vatNumber: '123456789', expected: false },
  ])('the VAT number "$vatNumber" of the SIREN 123456789 is valid: $expected', ({ vatNumber, expected }) => {
    // Act
    const valid = isValidVatNumber(vatNumber, '123456789');
    // Assert
    expect(valid).toBe(expected);
  });
});
