import { describe, expect, it } from 'vitest';
import { twoLinesAdminOrder } from '@/components/orders/orderFixture';
import { matchesOrderFilter, noOrderFilter } from './adminOrderFilter';

// Placed by John Smith, delivered to Jane Doe, PROCESSING, number 46555C9F
const order = twoLinesAdminOrder;

describe('adminOrderFilter', () => {
  it.each([
    { text: '', matches: true },
    { text: '46555c9f', matches: true },
    { text: 'smith', matches: true },
    { text: 'john.smith@example', matches: true },
    { text: 'jane doe', matches: true },
    { text: 'john 46555', matches: true },
    { text: 'martin', matches: false },
  ])('the search "$text" matches the order: $matches', ({ text, matches }) => {
    // Act
    const matching = matchesOrderFilter(order, { ...noOrderFilter, text });
    // Assert
    expect(matching).toBe(matches);
  });

  it.each([
    { status: null, matches: true },
    { status: 'PROCESSING' as const, matches: true },
    { status: 'SHIPPED' as const, matches: false },
  ])('the status $status matches the order: $matches', ({ status, matches }) => {
    // Act
    const matching = matchesOrderFilter(order, { ...noOrderFilter, status });
    // Assert
    expect(matching).toBe(matches);
  });
});
