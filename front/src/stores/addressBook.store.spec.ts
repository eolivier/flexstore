import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAddressBookStore } from './addressBook.store';
import type { PostalAddress } from '@/models/account';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const workshop: PostalAddress = {
  recipientType: 'PRIVATE_INDIVIDUAL',
  firstName: 'Jane',
  lastName: 'Doe',
  companyName: '',
  vatNumber: '',
  street: "5 rue de l'Atelier",
  complement: '',
  postalCode: '75011',
  city: 'Paris',
  country: 'France',
};

describe('addressBook.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('adds a new address, at the end of the addresses', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 201, json: () => Promise.resolve([workshop]) });
    const store = useAddressBookStore();
    // Act
    const addition = await store.addAddress(workshop);
    // Assert
    expect(addition).toBe('saved');
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringMatching(/\/api\/accounts\/me\/addresses$/),
      expect.objectContaining({ method: 'POST', body: JSON.stringify(workshop) })
    );
    expect(store.addresses).toEqual([workshop]);
  });

  it('reports an incomplete address without adding it', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 400 });
    // Act
    const addition = await useAddressBookStore().addAddress(workshop);
    // Assert
    expect(addition).toBe('incomplete');
  });

  it('reports an error when the address could not be added', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    // Act
    const addition = await useAddressBookStore().addAddress(workshop);
    // Assert
    expect(addition).toBe('error');
  });
});
