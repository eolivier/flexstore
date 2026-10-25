import { afterAll, beforeAll, describe, expect, it, vi } from 'vitest';
import type { JsonOrder } from '@/generated';
import {
  hasNextStatus,
  nextStatus,
  orderItemsCount,
  orderNumber,
  orderStepState,
  paymentDeadline,
  placedOn,
} from './order';
import { twoLinesOrder } from '@/components/orders/orderFixture';

const order: JsonOrder = {
  ...twoLinesOrder,
  id: '7687ab84-a48e-49ee-80b3-d218997b31d7',
  lines: [],
  totalPrice: 0,
};

describe('order utils', () => {
  // The times are shown in the time zone of the browser: the tests fix it
  beforeAll(() => {
    vi.stubEnv('TZ', 'UTC');
  });
  afterAll(() => {
    vi.unstubAllEnvs();
  });

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
    { locale: 'fr', expected: '24 octobre à 10:10' },
    { locale: 'en', expected: 'October 24 at 10:10 AM' },
  ])('the payment deadline in $locale is "$expected"', ({ locale, expected }) => {
    // Arrange
    const awaitingOrder: JsonOrder = {
      ...order,
      status: 'AWAITING_PAYMENT',
      paymentDeadline: '2026-10-24T10:10:00Z',
    };
    // Act
    const deadline = paymentDeadline(awaitingOrder, locale);
    // Assert
    expect(deadline).toBe(expected);
  });

  it('an order without payment deadline has none to show', () => {
    // Act
    const deadline = paymentDeadline(order, 'fr');
    // Assert
    expect(deadline).toBeNull();
  });

  it.each([
    { currentStatus: 'PROCESSING', expected: ['current', 'upcoming', 'upcoming'] },
    { currentStatus: 'SHIPPED', expected: ['done', 'current', 'upcoming'] },
    { currentStatus: 'DELIVERED', expected: ['done', 'done', 'current'] },
  ] as const)('an order $currentStatus has the steps $expected', ({ currentStatus, expected }) => {
    // Act
    const states = (['PROCESSING', 'SHIPPED', 'DELIVERED'] as const).map((step) =>
      orderStepState(step, currentStatus)
    );
    // Assert
    expect(states).toEqual(expected);
  });

  it('the item count adds up the quantities of the lines', () => {
    // Act
    const itemsCount = orderItemsCount(twoLinesOrder);
    // Assert
    expect(itemsCount).toBe(3);
  });

  it.each([
    { status: 'PROCESSING' as const, next: 'SHIPPED', hasNext: true },
    { status: 'SHIPPED' as const, next: 'DELIVERED', hasNext: true },
    { status: 'DELIVERED' as const, next: 'DELIVERED', hasNext: false },
  ])('an order $status is followed by $next (has a next status: $hasNext)', ({ status, next, hasNext }) => {
    // Act
    const following = [nextStatus(status), hasNextStatus(status)];
    // Assert
    expect(following).toEqual([next, hasNext]);
  });
});
