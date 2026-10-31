import { describe, expect, it } from 'vitest';
import { averageOrderValue, barHeightPercent } from './sales';

describe('sales', () => {
  it.each([
    { revenue: 300, orderCount: 4, expected: 75 },
    { revenue: 0, orderCount: 0, expected: 0 },
  ])('$orderCount orders bringing $revenue average $expected', ({ revenue, orderCount, expected }) => {
    // Act
    const average = averageOrderValue(revenue, orderCount);
    // Assert
    expect(average).toBe(expected);
  });

  it.each([
    { orderCount: 0, busiest: 8, expected: 0 },
    { orderCount: 8, busiest: 8, expected: 100 },
    { orderCount: 4, busiest: 8, expected: 50 },
    { orderCount: 1, busiest: 100, expected: 4 },
    { orderCount: 0, busiest: 0, expected: 0 },
  ])('$orderCount orders on a busiest day of $busiest make a bar of $expected %', (testCase) => {
    // Act
    const height = barHeightPercent(testCase.orderCount, testCase.busiest);
    // Assert
    expect(height).toBe(testCase.expected);
  });
});
