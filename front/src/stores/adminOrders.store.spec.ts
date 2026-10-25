import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAdminOrdersStore } from './adminOrders.store';
import { twoLinesOrder } from '@/components/orders/orderFixture';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const shippedOrder = { ...twoLinesOrder, status: 'SHIPPED' as const };

function respondWith(status: number, body?: unknown) {
  mockFetch.mockResolvedValue({ ok: status < 300, status, json: () => Promise.resolve(body) });
}

describe('adminOrders.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('loads the orders of all the customers', async () => {
    // Arrange
    respondWith(200, [twoLinesOrder]);
    const store = useAdminOrdersStore();
    // Act
    await store.fetchAllOrders();
    // Assert
    expect(store.orders).toEqual([twoLinesOrder]);
  });

  it('moves an order from the status shown to the next one', async () => {
    // Arrange
    respondWith(200, [twoLinesOrder]);
    const store = useAdminOrdersStore();
    await store.fetchAllOrders();
    respondWith(200, shippedOrder);
    // Act
    const statusAdvance = await store.advanceStatus(twoLinesOrder);
    // Assert
    const [url, init] = mockFetch.mock.calls[mockFetch.mock.calls.length - 1];
    expect(url).toContain(`/api/admin/orders/${twoLinesOrder.id}/status/advance`);
    expect(JSON.parse(init.body)).toEqual({ currentStatus: 'PROCESSING' });
    expect(statusAdvance).toBe('advanced');
    expect(store.orders).toEqual([shippedOrder]);
  });

  it.each([
    { status: 409, expected: 'statusChanged' },
    { status: 500, expected: 'error' },
  ])('a move answered $status is $expected', async ({ status, expected }) => {
    // Arrange
    respondWith(status);
    const store = useAdminOrdersStore();
    // Act
    const statusAdvance = await store.advanceStatus(twoLinesOrder);
    // Assert
    expect(statusAdvance).toBe(expected);
  });
});
