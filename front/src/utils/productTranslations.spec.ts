import { describe, expect, it } from 'vitest';
import { emptyTexts, isHalfEntered, textsOf, translationsOf } from './productTranslations';

describe('productTranslations', () => {
  it('gives empty texts for the languages a product is not translated into', () => {
    // Act
    const texts = textsOf([{ language: 'en', name: 'Lamp', description: 'Warm light' }]);
    // Assert
    expect(texts).toEqual({
      en: { name: 'Lamp', description: 'Warm light' },
      fr: { name: '', description: '' },
    });
  });

  it.each([
    { fr: { name: '', description: '' }, languages: ['en'] },
    { fr: { name: 'Lampe', description: 'Lumière chaude' }, languages: ['en', 'fr'] },
  ])('translates the product into the languages entered: $languages', ({ fr, languages }) => {
    // Arrange
    const texts = { ...emptyTexts(), en: { name: 'Lamp', description: 'Warm light' }, fr };
    // Act
    const translations = translationsOf(texts);
    // Assert
    expect(translations.map((translation) => translation.language)).toEqual(languages);
  });

  it.each([
    { fr: { name: 'Lampe', description: '' }, halfEntered: true },
    { fr: { name: '', description: 'Lumière chaude' }, halfEntered: true },
    { fr: { name: 'Lampe', description: 'Lumière chaude' }, halfEntered: false },
    { fr: { name: ' ', description: '' }, halfEntered: false },
  ])('$fr is half entered: $halfEntered', ({ fr, halfEntered }) => {
    // Arrange
    const texts = { ...emptyTexts(), fr };
    // Act
    const isHalf = isHalfEntered(texts, 'fr');
    // Assert
    expect(isHalf).toBe(halfEntered);
  });
});
