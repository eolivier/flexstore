import { describe, expect, it } from 'vitest';
import type { JsonOrder } from '@/generated';
import { orderNumber, placedOn } from './order';

const order: JsonOrder = {
  id: '7687ab84-a48e-49ee-80b3-d218997b31d7',
  placedAt: '2026-10-24T10:00:00Z',
  status: 'PROCESSING',
  lines: [],
  totalPrice: 0,
  currency: '€',
};

describe('order utils', () => {
  it('the order number is the beginning of the id, in capitals', () => {
    // Act
    const number = orderNumber(order);
    // Assert
    expect(number).toBe('7687AB84');
  });

  it.each([
    { locale: 'fr', expected: '24 octobre 2026' },
    { locale: 'en', expected: 'October 24, 2026' },
  ])('the order date in $locale is "$expected"', ({ locale, expected }) => {
    // Act
    const date = placedOn(order, locale);
    // Assert
    expect(date).toBe(expected);
  });
});
