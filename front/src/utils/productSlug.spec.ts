import { describe, expect, it } from 'vitest';
import { productPath, slugOf } from './productSlug';

describe('slugOf', () => {
  it.each([
    ['Organic T-shirt', 'organic-t-shirt'],
    ['T-shirt en coton bio !', 't-shirt-en-coton-bio'],
    ['Écharpe à carreaux', 'echarpe-a-carreaux'],
    ['  --Slim   Jeans--  ', 'slim-jeans'],
    ['', ''],
  ])('%s should become %s', (name, slug) => {
    // Act
    const result = slugOf(name);
    // Assert
    expect(result).toBe(slug);
  });

  it('should cut a long name at a word', () => {
    // Arrange
    const name = 'extraordinarily '.repeat(10);
    // Act
    const slug = slugOf(name);
    // Assert
    expect(slug.length).toBeLessThanOrEqual(60);
    expect(slug).toBe('extraordinarily-extraordinarily-extraordinarily');
  });
});

describe('productPath', () => {
  it('should put the id, then the slug of the name', () => {
    // Act
    const path = productPath({ id: 'jeans-1', name: 'Slim Jeans' });
    // Assert
    expect(path).toBe('/products/jeans-1/slim-jeans');
  });

  it('should be the id alone for a product without a name', () => {
    // Act
    const path = productPath({ id: 'jeans-1' });
    // Assert
    expect(path).toBe('/products/jeans-1');
  });
});
