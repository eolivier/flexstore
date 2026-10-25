import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useOrdersStore } from './orders.store';
import { useAuthStore } from './auth.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const order = {
  id: '3f2b8c1e-0000-0000-0000-000000000000',
  placedAt: '2026-10-24T10:00:00Z',
  status: 'PROCESSING',
  lines: [{ productId: '6', productName: 'Laptop', unitPrice: 799, quantity: 1, linePrice: 799 }],
  totalPrice: 799,
  currency: '€',
};
const homeAddresses = {
  deliveryAddress: {
    recipientType: 'PRIVATE_INDIVIDUAL' as const,
    firstName: 'Jane',
    lastName: 'Doe',
    street: '12 rue des Lilas',
    complement: '',
    postalCode: '75011',
    city: 'Paris',
    country: 'France',
  },
};
const emptyCart = { items: [], totalItemsPrice: 0, itemsCurrency: '€' };

// Answers each API call by method and path
function respondWith(responses: Record<string, { status: number; body?: unknown }>) {
  mockFetch.mockImplementation((url: string, init?: RequestInit) => {
    const key = Object.keys(responses).find((k) => {
      const [method, path] = k.split(' ');
      return (init?.method ?? 'GET') === method && url.endsWith(path);
    });
    const { status, body } = key ? responses[key] : { status: 404, body: undefined };
    return Promise.resolve({ ok: status < 300, status, json: () => Promise.resolve(body) });
  });
}

async function signIn() {
  respondWith({
    'POST /api/users/login': {
      status: 200,
      body: {
        token: 'jwt-token',
        expiresAt: '2030-01-01T00:00:00Z',
        user: {
          id: 'u-1',
          firstName: 'John',
          lastName: 'Doe',
          email: 'john@example.com',
          role: 'USER',
        },
      },
    },
  });
  await useAuthStore().login('john@example.com', 'secret');
}

describe('orders.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    localStorage.clear();
  });

  it('should load the orders of the signed-in customer', async () => {
    // Arrange
    await signIn();
    respondWith({ 'GET /api/orders': { status: 200, body: [order] } });
    const store = useOrdersStore();
    // Act
    await store.fetchOrders();
    // Assert
    expect(store.orders).toEqual([order]);
    const [, init] = mockFetch.mock.calls.find(([url]) => url.endsWith('/api/orders'))!;
    expect(new Headers(init.headers).get('Authorization')).toBe('Bearer jwt-token');
  });

  it('should place an order awaiting its payment, keeping the cart', async () => {
    // Arrange
    await signIn();
    respondWith({ 'POST /api/orders': { status: 201, body: order } });
    const store = useOrdersStore();
    // Act
    const placement = await store.placeOrder(homeAddresses);
    // Assert
    expect(placement).toEqual({ status: 'placed', order });
    expect(mockFetch.mock.calls.some(([url]) => url.endsWith('/api/cart/cart-items'))).toBe(false);
  });

  it.each([
    {
      status: 201,
      expected: { status: 'started', checkoutUrl: 'https://checkout.stripe.com/c/pay/cs_1' },
    },
    { status: 409, expected: { status: 'alreadyPaid' } },
    { status: 500, expected: { status: 'error' } },
  ])('a payment answered $status is $expected.status', async ({ status, expected }) => {
    // Arrange
    await signIn();
    const paymentSession = {
      checkoutUrl: 'https://checkout.stripe.com/c/pay/cs_1',
      outcome: 'PENDING',
    };
    respondWith({ [`POST /api/orders/${order.id}/payment`]: { status, body: paymentSession } });
    const store = useOrdersStore();
    // Act
    const paymentStart = await store.startPayment(order.id);
    // Assert
    expect(paymentStart).toEqual(expected);
  });

  it('should send the addresses of the order', async () => {
    // Arrange
    await signIn();
    respondWith({
      'POST /api/orders': { status: 201, body: order },
      'GET /api/cart/cart-items': { status: 200, body: emptyCart },
    });
    const store = useOrdersStore();
    // Act
    await store.placeOrder(homeAddresses);
    // Assert
    const [, init] = mockFetch.mock.calls.find(
      ([url, init]) => url.endsWith('/api/orders') && init?.method === 'POST'
    )!;
    expect(JSON.parse(init.body)).toEqual(homeAddresses);
  });

  it.each([
    { status: 422, expected: 'emptyCart' },
    { status: 403, expected: 'refused' },
    { status: 400, expected: 'incompleteAddress' },
    { status: 500, expected: 'error' },
  ])('an order answered $status is $expected', async ({ status, expected }) => {
    // Arrange
    await signIn();
    respondWith({ 'POST /api/orders': { status } });
    const store = useOrdersStore();
    // Act
    const placement = await store.placeOrder(homeAddresses);
    // Assert
    expect(placement).toEqual({ status: expected });
  });

  it('should report a loading error', async () => {
    // Arrange
    await signIn();
    respondWith({ 'GET /api/orders': { status: 500 } });
    const store = useOrdersStore();
    // Act
    await store.fetchOrders();
    // Assert
    expect(store.loadError).toBe(true);
  });

  it.each([
    { status: 200, expected: { status: 'found', order } },
    { status: 404, expected: { status: 'notFound' } },
    { status: 500, expected: { status: 'error' } },
  ])(
    'should look up an order: HTTP $status gives $expected.status',
    async ({ status, expected }) => {
      // Arrange
      await signIn();
      respondWith({
        [`GET /api/orders/${order.id}`]: { status, body: status === 200 ? order : undefined },
      });
      const store = useOrdersStore();
      // Act
      const orderLookup = await store.fetchOrder(order.id);
      // Assert
      expect(orderLookup).toEqual(expected);
    }
  );
});
