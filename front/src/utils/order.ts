import type { JsonOrder } from '@/generated';

// Short number shown to the customer: the beginning of the order id
export function orderNumber(order: JsonOrder): string {
  return order.id.slice(0, 8).toUpperCase();
}

export function orderItemsCount(order: JsonOrder): number {
  return order.lines.reduce((count, line) => count + line.quantity, 0);
}

export function placedOn(order: JsonOrder, locale: string): string {
  return new Date(order.placedAt).toLocaleDateString(locale, { day: 'numeric', month: 'long', year: 'numeric' });
}

// full: every step with its label (order detail); compact: smaller markers, only the current step labelled (orders list)
export type OrderStepsLayout = 'full' | 'compact';

// The statuses an order goes through, in order
export const orderStatusSteps: JsonOrder['status'][] = ['PROCESSING', 'SHIPPED', 'DELIVERED'];

export type OrderStepState = 'done' | 'current' | 'upcoming';

// Where a step stands for an order in the given status
export function orderStepState(step: JsonOrder['status'], currentStatus: JsonOrder['status']): OrderStepState {
  const stepIndex = orderStatusSteps.indexOf(step);
  const currentIndex = orderStatusSteps.indexOf(currentStatus);
  if (stepIndex < currentIndex) return 'done';
  if (stepIndex === currentIndex) return 'current';
  return 'upcoming';
}
