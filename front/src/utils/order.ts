import type { JsonOrder } from '@/generated';

// Short number shown to the customer: the beginning of the order id
export function orderNumber(order: JsonOrder): string {
  return order.id.slice(0, 8).toUpperCase();
}

export function placedOn(order: JsonOrder, locale: string): string {
  return new Date(order.placedAt).toLocaleDateString(locale, { day: 'numeric', month: 'long', year: 'numeric' });
}

export const orderStatusColors: Record<JsonOrder['status'], string> = {
  PROCESSING: 'warning',
  SHIPPED: 'info',
  DELIVERED: 'success',
};
