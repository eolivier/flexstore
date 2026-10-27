import { describe, expect, it } from 'vitest';
import type { Product } from '@/models/product';
import { matchesCatalogSearch, matchesSearch } from './productSearch';

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

// The names of the categories, as the catalog gives them in French
function categoryNameInFrench(category: string): string {
  return category === 'CLOTHING' ? 'Vêtements' : '';
}

const anyProduct = { ...jacket, category: 'UNKNOWN' } as Product;

describe('catalog search', () => {
  it.each([
    { search: 'vetements', product: jacket, matches: true },
    { search: 'jacket vêtements', product: jacket, matches: true },
    { search: 'legere', product: jacket, matches: true },
    { search: 'vetements', product: anyProduct, matches: false },
    { search: 'electronique', product: jacket, matches: false },
  ])('the search "$search" matches $product.category: $matches', ({ search, product, matches }) => {
    // Arrange
    const catalogSearch = { text: search, categoryName: categoryNameInFrench };
    // Act
    const matched = matchesCatalogSearch(product, catalogSearch);
    // Assert
    expect(matched).toBe(matches);
  });
});
