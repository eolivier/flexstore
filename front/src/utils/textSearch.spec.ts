import { describe, expect, it } from 'vitest';
import { containsEveryWord } from './textSearch';

describe('textSearch', () => {
  it.each(['vetement', 'VÊTEMENT', 'vêtement'])('the search "%s" should find "Vêtement"', (search) => {
    // Act
    const found = containsEveryWord('Vêtement', search);
    // Assert
    expect(found).toBe(true);
  });

  it('should find a text that has every word searched, in any order', () => {
    // Act
    const found = containsEveryWord('Organic cotton t-shirt', 'shirt organic');
    // Assert
    expect(found).toBe(true);
  });

  it('should not find a text that lacks one of the words searched', () => {
    // Act
    const found = containsEveryWord('Organic cotton t-shirt', 'organic wool');
    // Assert
    expect(found).toBe(false);
  });

  it.each(['', '   '])('the empty search "%s" should find any text', (search) => {
    // Act
    const found = containsEveryWord('Organic cotton t-shirt', search);
    // Assert
    expect(found).toBe(true);
  });

  it('should ignore the spaces around the words searched', () => {
    // Act
    const found = containsEveryWord('Organic cotton t-shirt', '  organic   cotton ');
    // Assert
    expect(found).toBe(true);
  });

  it('should find nothing in an empty text but the empty search', () => {
    // Act
    const found = containsEveryWord('', 'organic');
    // Assert
    expect(found).toBe(false);
  });
});
