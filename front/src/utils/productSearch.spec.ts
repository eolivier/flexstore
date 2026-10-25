import { describe, expect, it } from 'vitest';
import type { Product } from '@/models/product';
import { matchesSearch } from './productSearch';

const jacket = {
  id: 'p-1',
  name: 'Waterproof Jacket',
  description: 'Légère et imperméable',
  category: 'CLOTHING',
  price: 89,
  currency: 'EUR',
} as Product;

describe('productSearch', () => {
  it.each([
    { search: '', matches: true },
    { search: 'jacket', matches: true },
    { search: 'WATERPROOF', matches: true },
    { search: 'legere', matches: true },
    { search: 'jacket impermeable', matches: true },
    { search: 'jacket blue', matches: false },
    { search: 'shoes', matches: false },
  ])('the search "$search" matches the jacket: $matches', ({ search, matches }) => {
    // Act
    const result = matchesSearch(jacket, search);
    // Assert
    expect(result).toBe(matches);
  });
});
