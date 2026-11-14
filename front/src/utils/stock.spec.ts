import { describe, expect, it } from 'vitest';
import { stockStateOf } from './stock';

describe('stock', () => {
  it('no unit left should be out of stock', () => {
    // Act
    const stockState = stockStateOf(0);
    // Assert
    expect(stockState).toBe('outOfStock');
  });

  it.each([1, 3, 5])('%i units left should be a few left', (quantity) => {
    // Act
    const stockState = stockStateOf(quantity);
    // Assert
    expect(stockState).toBe('fewLeft');
  });

  it.each([6, 50])('%i units left should be in stock', (quantity) => {
    // Act
    const stockState = stockStateOf(quantity);
    // Assert
    expect(stockState).toBe('inStock');
  });
});
