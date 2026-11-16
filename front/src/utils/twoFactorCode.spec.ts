import { describe, expect, it } from 'vitest';
import { formattedTwoFactorCode, isCompleteTwoFactorCode, twoFactorCodeDigits } from './twoFactorCode';

describe('twoFactorCode', () => {
  describe('formattedTwoFactorCode', () => {
    it('puts a space between the first 3 digits and the last 3', () => {
      expect(formattedTwoFactorCode('123456')).toBe('123 456');
    });

    it('adds no space while only 3 digits or fewer are typed', () => {
      expect(formattedTwoFactorCode('123')).toBe('123');
    });

    it('starts the second group with the fourth digit', () => {
      expect(formattedTwoFactorCode('1234')).toBe('123 4');
    });

    it('keeps the format of a code already formatted', () => {
      expect(formattedTwoFactorCode('123 456')).toBe('123 456');
    });

    it('drops what is not a digit and the digits past the sixth', () => {
      expect(formattedTwoFactorCode('12a3-4567 89')).toBe('123 456');
    });
  });

  describe('twoFactorCodeDigits', () => {
    it('keeps only the digits, without the space', () => {
      expect(twoFactorCodeDigits('123 456')).toBe('123456');
    });
  });

  describe('isCompleteTwoFactorCode', () => {
    it('is true once 6 digits are typed, spaces apart', () => {
      expect(isCompleteTwoFactorCode('123 456')).toBe(true);
    });

    it('is false with fewer than 6 digits', () => {
      expect(isCompleteTwoFactorCode('123 45')).toBe(false);
    });
  });
});
