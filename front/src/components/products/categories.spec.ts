import { describe, expect, it } from 'vitest';
import { isKnownCategory } from './categories';

describe('categories', () => {
  it.each([
    { category: 'CLOTHING', known: true },
    { category: 'ELECTRONICS', known: true },
    { category: 'ACCESSORIES', known: true },
    { category: 'GARDEN', known: false },
  ])('the category $category is known: $known', ({ category, known }) => {
    // Act
    const isKnown = isKnownCategory(category);
    // Assert
    expect(isKnown).toBe(known);
  });
});
