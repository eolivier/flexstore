import { describe, expect, it } from 'vitest';
import { formatAddress } from './address';

describe('formatAddress', () => {
  it.each([
    {
      address: {
        recipientType: 'PRIVATE_INDIVIDUAL' as const,
        firstName: 'Jane',
        lastName: 'Doe',
        street: '12 rue des Lilas',
        complement: 'Bâtiment B',
        postalCode: '75011',
        city: 'Paris',
        country: 'France',
      },
      expected: 'Jane Doe, 12 rue des Lilas, Bâtiment B, 75011 Paris, France',
    },
    {
      address: {
        recipientType: 'PRIVATE_INDIVIDUAL' as const,
        firstName: 'Jane',
        lastName: 'Doe',
        street: '3 place du Marché',
        complement: '',
        postalCode: '69001',
        city: 'Lyon',
        country: 'France',
      },
      expected: 'Jane Doe, 3 place du Marché, 69001 Lyon, France',
    },
    {
      address: {
        recipientType: 'PRIVATE_INDIVIDUAL' as const,
        firstName: 'Jane',
        lastName: 'Doe',
        street: '3 place du Marché',
        complement: null,
        postalCode: '69001',
        city: 'Lyon',
        country: 'France',
      },
      expected: 'Jane Doe, 3 place du Marché, 69001 Lyon, France',
    },
    {
      address: {
        recipientType: 'LEGAL_ENTITY',
        companyName: 'Acme',
        vatNumber: 'FR12345678901',
        street: '3 place du Marché',
        complement: null,
        postalCode: '69001',
        city: 'Lyon',
        country: 'France',
      },
      expected: 'Acme, 3 place du Marché, 69001 Lyon, France',
    },
  ])('$address reads "$expected"', ({ address, expected }) => {
    // Act
    const line = formatAddress(address);
    // Assert
    expect(line).toBe(expected);
  });
});
