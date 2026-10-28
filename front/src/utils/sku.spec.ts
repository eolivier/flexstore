import { describe, expect, it } from 'vitest';
import { isWellFormedSku, normalizedSku } from './sku';

describe('sku', () => {
  it('a typed SKU should be trimmed and put in upper case', () => {
    // Act
    const sku = normalizedSku('  tshirt-organic-m ');
    // Assert
    expect(sku).toBe('TSHIRT-ORGANIC-M');
  });

  it.each(['A', 'CLO-1', 'tshirt-organic-m', ' MUG-12 ', 'A23456789-123456789-123456789-12'])(
    '"%s" is well formed',
    (typedSku) => {
      // Act
      const wellFormed = isWellFormedSku(typedSku);
      // Assert
      expect(wellFormed).toBe(true);
    }
  );

  it.each(['', 'T SHIRT', 'T--SHIRT', '-SHIRT', 'SHIRT-', 'CAFÉ', 'A23456789-123456789-123456789-123'])(
    '"%s" is malformed',
    (typedSku) => {
      // Act
      const wellFormed = isWellFormedSku(typedSku);
      // Assert
      expect(wellFormed).toBe(false);
    }
  );
});
