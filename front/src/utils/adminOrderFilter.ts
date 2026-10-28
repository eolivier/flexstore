import type { JsonAdminOrder } from '@/generated';
import { recipientName } from './address';
import { orderNumber } from './order';
import { containsEveryWord } from './textSearch';

// What the administrator looks for: words in the number of the order, the name or the email of its customer, or
// the name of its recipient; a status (null: any)
export interface AdminOrderFilter {
  text: string;
  status: JsonAdminOrder['status'] | null;
}

export const noOrderFilter: AdminOrderFilter = { text: '', status: null };

export function matchesOrderFilter(order: JsonAdminOrder, filter: AdminOrderFilter): boolean {
  const { firstName, lastName, email } = order.customer;
  const searchedText = [orderNumber(order), firstName, lastName, email, recipientName(order.deliveryAddress)];
  const matchesText = containsEveryWord(searchedText.join(' '), filter.text);
  const matchesStatus = !filter.status || order.status === filter.status;
  return matchesText && matchesStatus;
}
