import { describe, expect, it } from 'vitest';
import { generateDiscountCode } from './discountCode';

describe('generateDiscountCode', () => {
  it('should generate a code of 8 upper-case letters and digits, accepted as a discount code', () => {
    // Act
    const code = generateDiscountCode();
    // Assert
    expect(code).toMatch(/^[A-Z0-9]{8}$/);
  });

  it('should pick each character from the random value it is given', () => {
    // Arrange
    const randomValues = (length: number) => new Uint32Array(length).fill(0);
    // Act
    const code = generateDiscountCode(randomValues);
    // Assert
    expect(code).toBe('AAAAAAAA');
  });

  it('should not generate characters that look alike', () => {
    // Act
    const codes = Array.from({ length: 200 }, () => generateDiscountCode()).join('');
    // Assert
    expect(codes).not.toMatch(/[01OI]/);
  });
});
