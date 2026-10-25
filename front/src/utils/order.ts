import type { JsonOrder } from '@/generated';

// Short number shown to the customer: the beginning of the order id
export function orderNumber(order: JsonOrder): string {
  return order.id.slice(0, 8).toUpperCase();
}

export function orderItemsCount(order: JsonOrder): number {
  return order.lines.reduce((count, line) => count + line.quantity, 0);
}

export function placedOn(order: JsonOrder, locale: string): string {
  return new Date(order.placedAt).toLocaleDateString(locale, {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
}

// full: every step with its label (order detail);
// compact: smaller markers, only the current step labelled (orders list)
export type OrderStepsLayout = 'full' | 'compact';

// The statuses an order goes through, in order
export const orderStatusSteps: JsonOrder['status'][] = [
  'AWAITING_PAYMENT',
  'PAID',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
];

export type OrderStepState = 'done' | 'current' | 'upcoming';

// A delivered order has no next status
export function hasNextStatus(status: JsonOrder['status']): boolean {
  return status !== 'DELIVERED';
}

// The payment moves an order awaiting it, the administrators move it along the next statuses
export function isMovedByAnAdministrator(status: JsonOrder['status']): boolean {
  return status !== 'AWAITING_PAYMENT' && hasNextStatus(status);
}

// The status that follows in the life cycle of an order (the last one stays as it is)
export function nextStatus(status: JsonOrder['status']): JsonOrder['status'] {
  const nextIndex = Math.min(orderStatusSteps.indexOf(status) + 1, orderStatusSteps.length - 1);
  return orderStatusSteps[nextIndex];
}

// Where a step stands for an order in the given status
export function orderStepState(step: JsonOrder['status'], currentStatus: JsonOrder['status']): OrderStepState {
  const stepIndex = orderStatusSteps.indexOf(step);
  const currentIndex = orderStatusSteps.indexOf(currentStatus);
  if (stepIndex < currentIndex) return 'done';
  if (stepIndex === currentIndex) return 'current';
  return 'upcoming';
}
