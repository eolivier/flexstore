import { describe, expect, it } from 'vitest';
import { matchesFilter, noFilter } from './productFilter';

const jacket = {
  id: 'p-2',
  name: 'Waterproof Jacket',
  description: 'Resists rain and wind',
  category: 'CLOTHING',
  price: 79,
  currency: '€',
};

describe('matchesFilter', () => {
  it.each([
    { filter: { ...noFilter }, stock: 3, matches: true },
    { filter: { ...noFilter, text: 'jacket' }, stock: 3, matches: true },
    { filter: { ...noFilter, text: 'RAIN' }, stock: 3, matches: true },
    { filter: { ...noFilter, text: 'rain jacket' }, stock: 3, matches: true },
    { filter: { ...noFilter, text: 'jacket snow' }, stock: 3, matches: false },
    { filter: { ...noFilter, text: 'laptop' }, stock: 3, matches: false },
    { filter: { ...noFilter, category: 'CLOTHING' }, stock: 3, matches: true },
    { filter: { ...noFilter, category: 'ELECTRONICS' }, stock: 3, matches: false },
    { filter: { ...noFilter, stockState: 'fewLeft' as const }, stock: 3, matches: true },
    { filter: { ...noFilter, stockState: 'outOfStock' as const }, stock: 3, matches: false },
    { filter: { ...noFilter, stockState: 'outOfStock' as const }, stock: 0, matches: true },
    { filter: { ...noFilter, stockState: 'inStock' as const }, stock: 6, matches: true },
    { filter: { ...noFilter, stockState: 'inStock' as const }, stock: null, matches: false },
  ])('$filter with a stock of $stock matches: $matches', ({ filter, stock, matches }) => {
    // Act
    const matched = matchesFilter(jacket, stock, filter);
    // Assert
    expect(matched).toBe(matches);
  });
});
