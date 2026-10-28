import { describe, expect, it } from 'vitest';
import type { Product } from '@/models/product';
import { productTextIn } from './productText';

const jacket: Product = {
  id: '2',
  name: 'Waterproof Jacket',
  description: 'Resists rain and wind',
  category: 'CLOTHING',
  price: 79,
  currency: '€',
  translations: [
    { language: 'en', name: 'Waterproof Jacket', description: 'Resists rain and wind' },
    { language: 'fr', name: 'Veste imperméable', description: 'Résiste à la pluie et au vent' },
  ],
};

describe('productTextIn', () => {
  it.each([
    { language: 'fr', name: 'Veste imperméable' },
    { language: 'en', name: 'Waterproof Jacket' },
  ])('in $language, the jacket is called $name', ({ language, name }) => {
    // Act
    const text = productTextIn(jacket, language);
    // Assert
    expect(text.name).toBe(name);
  });

  it('should give the default name and description of a product not translated into the language', () => {
    // Arrange
    const englishOnlyJacket: Product = { ...jacket, translations: [] };
    // Act
    const text = productTextIn(englishOnlyJacket, 'fr');
    // Assert
    expect(text).toEqual({ name: 'Waterproof Jacket', description: 'Resists rain and wind' });
  });
});
