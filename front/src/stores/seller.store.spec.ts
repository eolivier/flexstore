import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { noSocialNetworkUrls } from '@/utils/socialNetworks';
import { useSellerStore, type Seller } from './seller.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const flexstoreSeller: Seller = {
  name: 'Flexstore',
  address: '12 rue des Lilas, 75011 Paris',
  siret: '12345678901234',
  vatNumber: 'FR12345678901',
  phone: '+33102030405',
  email: 'contact@flexstore.example',
  website: 'https://flexstore.example',
  legalForm: 'SAS',
  shareCapital: '10000',
  registryCity: 'Paris',
  socialNetworks: noSocialNetworkUrls(),
};
const answer = (status: number, body?: unknown) =>
  mockFetch.mockResolvedValueOnce({ ok: status < 300, status, json: () => Promise.resolve(body) });

describe('seller.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should know no seller before it is loaded', () => {
    // Act
    const store = useSellerStore();
    // Assert
    expect(store.seller).toBeNull();
    expect(store.loadError).toBe(false);
  });

  it('should load the seller', async () => {
    // Arrange
    answer(200, flexstoreSeller);
    const store = useSellerStore();
    // Act
    await store.fetchSeller();
    // Assert
    expect(store.seller).toEqual(flexstoreSeller);
    expect(store.loadError).toBe(false);
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/seller');
  });

  it('should report an error when the seller cannot be loaded', async () => {
    // Arrange
    answer(500);
    const store = useSellerStore();
    // Act
    await store.fetchSeller();
    // Assert
    expect(store.loadError).toBe(true);
    expect(store.seller).toBeNull();
  });

  it('should report an error when the API cannot be reached to load the seller', async () => {
    // Arrange
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    const store = useSellerStore();
    // Act
    await store.fetchSeller();
    // Assert
    expect(store.loadError).toBe(true);
  });

  it('should forget the previous error when the seller is loaded again', async () => {
    // Arrange
    answer(500);
    const store = useSellerStore();
    await store.fetchSeller();
    answer(200, flexstoreSeller);
    // Act
    await store.fetchSeller();
    // Assert
    expect(store.loadError).toBe(false);
    expect(store.seller).toEqual(flexstoreSeller);
  });

  it('should keep the seller returned by the shop when it is updated', async () => {
    // Arrange
    const updatedSeller = { ...flexstoreSeller, name: 'Flexstore SAS' };
    answer(200, updatedSeller);
    const store = useSellerStore();
    // Act
    const update = await store.updateSeller(updatedSeller);
    // Assert
    expect(update).toBe('saved');
    expect(store.seller).toEqual(updatedSeller);
    expect(mockFetch.mock.calls[0][0]).toMatch(/\/api\/admin\/seller$/);
    expect(mockFetch.mock.calls[0][1].method).toBe('PUT');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual(updatedSeller);
  });

  it('should save the seller at the setup address when the setup is completed', async () => {
    // Arrange
    answer(200, flexstoreSeller);
    const store = useSellerStore();
    // Act
    const completion = await store.completeSetup(flexstoreSeller);
    // Assert
    expect(completion).toBe('saved');
    expect(store.seller).toEqual(flexstoreSeller);
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/seller/setup');
  });

  it('should refuse a seller that the shop finds invalid', async () => {
    // Arrange
    answer(400);
    const store = useSellerStore();
    // Act
    const update = await store.updateSeller(flexstoreSeller);
    // Assert
    expect(update).toBe('invalid');
    expect(store.seller).toBeNull();
  });

  it('should fail to update the seller when the shop fails', async () => {
    // Arrange
    answer(500);
    const store = useSellerStore();
    // Act
    const update = await store.updateSeller(flexstoreSeller);
    // Assert
    expect(update).toBe('failed');
    expect(store.seller).toBeNull();
  });

  it('should fail to complete the setup when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    const store = useSellerStore();
    // Act
    const completion = await store.completeSetup(flexstoreSeller);
    // Assert
    expect(completion).toBe('failed');
  });
});
