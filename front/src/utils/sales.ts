// What an order brings on average, 0 when there is no order
export function averageOrderValue(revenue: number, orderCount: number): number {
  return orderCount === 0 ? 0 : revenue / orderCount;
}

// The height of the bar of a day in the chart, in percent of the busiest day; a day with orders keeps a
// visible bar
export function barHeightPercent(orderCount: number, busiestDayOrderCount: number): number {
  if (orderCount === 0 || busiestDayOrderCount === 0) return 0;
  return Math.max(4, Math.round((orderCount / busiestDayOrderCount) * 100));
}
