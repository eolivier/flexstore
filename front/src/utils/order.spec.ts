import { describe, expect, it } from 'vitest';
import type { JsonOrder } from '@/generated';
import { orderItemsCount, orderNumber, orderStepState, placedOn } from './order';
import { twoLinesOrder } from '@/components/orders/orderFixture';

const order: JsonOrder = {
  ...twoLinesOrder,
  id: '7687ab84-a48e-49ee-80b3-d218997b31d7',
  lines: [],
  totalPrice: 0,
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

  it.each([
    { currentStatus: 'PROCESSING', expected: ['current', 'upcoming', 'upcoming'] },
    { currentStatus: 'SHIPPED', expected: ['done', 'current', 'upcoming'] },
    { currentStatus: 'DELIVERED', expected: ['done', 'done', 'current'] },
  ] as const)('an order $currentStatus has the steps $expected', ({ currentStatus, expected }) => {
    // Act
    const states = (['PROCESSING', 'SHIPPED', 'DELIVERED'] as const).map(step => orderStepState(step, currentStatus));
    // Assert
    expect(states).toEqual(expected);
  });

  it('the item count adds up the quantities of the lines', () => {
    // Act
    const itemsCount = orderItemsCount(twoLinesOrder);
    // Assert
    expect(itemsCount).toBe(3);
  });
});
