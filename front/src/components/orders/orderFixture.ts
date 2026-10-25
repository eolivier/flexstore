import type { JsonOrder } from '@/generated';

// An order of two lines, used by the component tests
export const twoLinesOrder: JsonOrder = {
  id: '46555c9f-4e92-4a00-88cc-03c88440cdb9',
  placedAt: '2026-10-24T10:00:00Z',
  status: 'PROCESSING',
  lines: [
    { productId: '4', productName: '5G Smartphone', unitPrice: 399, quantity: 2, linePrice: 798 },
    {
      productId: '8',
      productName: 'Bluetooth Headphones',
      unitPrice: 59,
      quantity: 1,
      linePrice: 59,
    },
  ],
  totalPrice: 857,
  currency: '€',
  deliveryAddress: {
    recipientType: 'PRIVATE_INDIVIDUAL' as const,
    firstName: 'Jane',
    lastName: 'Doe',
    street: '12 rue des Lilas',
    complement: null,
    postalCode: '75011',
    city: 'Paris',
    country: 'France',
  },
  billingAddress: {
    recipientType: 'PRIVATE_INDIVIDUAL' as const,
    firstName: 'Jane',
    lastName: 'Doe',
    street: '12 rue des Lilas',
    complement: null,
    postalCode: '75011',
    city: 'Paris',
    country: 'France',
  },
};
