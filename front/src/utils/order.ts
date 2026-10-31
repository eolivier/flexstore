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

// The time left to pay the order, "mm:ss" (or "h:mm:ss" from one hour), at the given moment; null when the order
// has no payment deadline, or once it is reached
export function timeLeftToPay(order: JsonOrder, now: Date): string | null {
  if (!order.paymentDeadline) return null;
  const secondsLeft = Math.ceil((new Date(order.paymentDeadline).getTime() - now.getTime()) / 1000);
  if (secondsLeft <= 0) return null;
  const hours = Math.floor(secondsLeft / 3600);
  const minutes = Math.floor((secondsLeft % 3600) / 60);
  const seconds = secondsLeft % 60;
  const minutesAndSeconds = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  return hours > 0 ? `${hours}:${minutesAndSeconds}` : minutesAndSeconds;
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

// An order cancelled because it was not paid in time, or cancelled and refunded by the shop, left its life
// cycle: it has no step any more
export function hasLeftItsLifeCycle(status: JsonOrder['status']): boolean {
  return status === 'CANCELLED' || status === 'REFUNDED';
}

// A delivered order has no next status, nor an order that left its life cycle
export function hasNextStatus(status: JsonOrder['status']): boolean {
  return status !== 'DELIVERED' && !hasLeftItsLifeCycle(status);
}

// A paid order has its invoice, kept when it is refunded afterwards
export function isInvoiced(status: JsonOrder['status']): boolean {
  return ['PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'REFUNDED'].includes(status);
}

// Paid, and not shipped yet: an administrator may cancel it and refund it
export function isRefundable(status: JsonOrder['status']): boolean {
  return status === 'PAID' || status === 'PROCESSING';
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
