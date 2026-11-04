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

const lastRequest = () => mockFetch.mock.calls[mockFetch.mock.calls.length - 1];

describe('adminOrders.store, tracking', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('ships an order with its carrier and its tracking number, without the spaces around the number', async () => {
    // Arrange
    respondWith(200, shippedOrder);
    const store = useAdminOrdersStore();
    // Act
    await store.advanceStatus(twoLinesOrder, { carrier: 'UPS', trackingNumber: ' 1Z999 ' });
    // Assert
    const [, init] = lastRequest();
    expect(JSON.parse(init.body)).toEqual({
      currentStatus: 'PROCESSING',
      carrier: 'UPS',
      trackingNumber: '1Z999',
    });
  });

  it('ships an order without tracking by sending only its status', async () => {
    // Arrange
    respondWith(200, shippedOrder);
    const store = useAdminOrdersStore();
    // Act
    await store.advanceStatus(twoLinesOrder);
    // Assert
    const [, init] = lastRequest();
    expect(JSON.parse(init.body)).toEqual({ currentStatus: 'PROCESSING' });
  });

  it('corrects the tracking of an order', async () => {
    // Arrange
    respondWith(200, shippedOrder);
    const store = useAdminOrdersStore();
    // Act
    const correction = await store.correctTracking(shippedOrder, { carrier: 'DHL', trackingNumber: 'JD01' });
    // Assert
    const [url, init] = lastRequest();
    expect(correction).toBe('corrected');
    expect(url).toBe(`http://localhost:8080/api/admin/orders/${twoLinesOrder.id}/tracking`);
    expect(init.method).toBe('PUT');
    expect(JSON.parse(init.body)).toEqual({ carrier: 'DHL', trackingNumber: 'JD01' });
  });

  it.each([
    { status: 409, expected: 'notCorrectable' },
    { status: 400, expected: 'invalid' },
    { status: 500, expected: 'error' },
  ])('tells a correction answered $status: $expected', async ({ status, expected }) => {
    // Arrange
    respondWith(status);
    const store = useAdminOrdersStore();
    // Act
    const correction = await store.correctTracking(shippedOrder, { carrier: 'DHL', trackingNumber: 'JD01' });
    // Assert
    expect(correction).toBe(expected);
  });
});
