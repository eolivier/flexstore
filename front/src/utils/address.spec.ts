import { describe, expect, it } from 'vitest';
import { formatAddress } from './address';

describe('formatAddress', () => {
  it.each([
    {
      address: { street: '12 rue des Lilas', complement: 'Bâtiment B', postalCode: '75011', city: 'Paris', country: 'France' },
      expected: '12 rue des Lilas, Bâtiment B, 75011 Paris, France',
    },
    { address: { street: '3 place du Marché', complement: '', postalCode: '69001', city: 'Lyon', country: 'France' }, expected: '3 place du Marché, 69001 Lyon, France' },
    { address: { street: '3 place du Marché', complement: null, postalCode: '69001', city: 'Lyon', country: 'France' }, expected: '3 place du Marché, 69001 Lyon, France' },
    { address: undefined, expected: '' },
  ])('$address reads "$expected"', ({ address, expected }) => {
    // Act
    const line = formatAddress(address);
    // Assert
    expect(line).toBe(expected);
  });
});
