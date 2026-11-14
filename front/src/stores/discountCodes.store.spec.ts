import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import type { JsonDiscountCode, JsonNewDiscountCode } from '@/generated';
import { useDiscountCodesStore } from './discountCodes.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const springCode = { code: 'SPRING10' } as JsonDiscountCode;
const summerCode = { code: 'SUMMER20' } as JsonDiscountCode;
const newDiscountCode = { code: 'SPRING10' } as JsonNewDiscountCode;
const answer = (status: number, body?: unknown) =>
  mockFetch.mockResolvedValueOnce({ ok: status < 300, status, json: () => Promise.resolve(body) });

describe('discountCodes.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should load the discount codes', async () => {
    // Arrange
    answer(200, [springCode, summerCode]);
    const store = useDiscountCodesStore();
    // Act
    await store.fetchDiscountCodes();
    // Assert
    expect(store.discountCodes).toEqual([springCode, summerCode]);
    expect(store.loading).toBe(false);
    expect(store.loadError).toBe(false);
  });

  it('should report an error when the discount codes cannot be loaded', async () => {
    // Arrange
    answer(500);
    const store = useDiscountCodesStore();
    // Act
    await store.fetchDiscountCodes();
    // Assert
    expect(store.loadError).toBe(true);
    expect(store.loading).toBe(false);
  });

  it('should report an error when the API cannot be reached', async () => {
    // Arrange
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    const store = useDiscountCodesStore();
    // Act
    await store.fetchDiscountCodes();
    // Assert
    expect(store.loadError).toBe(true);
  });

  it('should forget the previous error when the discount codes are loaded again', async () => {
    // Arrange
    answer(500);
    const store = useDiscountCodesStore();
    await store.fetchDiscountCodes();
    answer(200, [springCode]);
    // Act
    await store.fetchDiscountCodes();
    // Assert
    expect(store.loadError).toBe(false);
    expect(store.discountCodes).toEqual([springCode]);
  });

  it('should load the discount codes again once one is created', async () => {
    // Arrange
    answer(201);
    answer(200, [springCode]);
    const store = useDiscountCodesStore();
    // Act
    const creation = await store.createDiscountCode(newDiscountCode);
    // Assert
    expect(creation).toBe('created');
    expect(store.discountCodes).toEqual([springCode]);
    expect(mockFetch.mock.calls[0][1].method).toBe('POST');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual(newDiscountCode);
  });

  it.each([
    { status: 409, expected: 'alreadyExists' },
    { status: 400, expected: 'invalid' },
    { status: 500, expected: 'error' },
  ])('a creation answered by $status should be $expected', async ({ status, expected }) => {
    // Arrange
    answer(status);
    const store = useDiscountCodesStore();
    // Act
    const creation = await store.createDiscountCode(newDiscountCode);
    // Assert
    expect(creation).toBe(expected);
    expect(mockFetch).toHaveBeenCalledTimes(1);
  });

  it('should report an error when the API cannot be reached to create a discount code', async () => {
    // Arrange
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    const store = useDiscountCodesStore();
    // Act
    const creation = await store.createDiscountCode(newDiscountCode);
    // Assert
    expect(creation).toBe('error');
  });

  it('should remove a deleted discount code from the list', async () => {
    // Arrange
    answer(200, [springCode, summerCode]);
    const store = useDiscountCodesStore();
    await store.fetchDiscountCodes();
    answer(204);
    // Act
    const deleted = await store.deleteDiscountCode('SPRING10');
    // Assert
    expect(deleted).toBe(true);
    expect(store.discountCodes).toEqual([summerCode]);
    expect(mockFetch.mock.calls[1][0]).toContain('/api/admin/discount-codes/SPRING10');
    expect(mockFetch.mock.calls[1][1].method).toBe('DELETE');
  });

  it('should keep the discount code in the list when its deletion is refused', async () => {
    // Arrange
    answer(200, [springCode]);
    const store = useDiscountCodesStore();
    await store.fetchDiscountCodes();
    answer(404);
    // Act
    const deleted = await store.deleteDiscountCode('SPRING10');
    // Assert
    expect(deleted).toBe(false);
    expect(store.discountCodes).toEqual([springCode]);
  });

  it('should keep the discount code in the list when the API cannot be reached to delete it', async () => {
    // Arrange
    answer(200, [springCode]);
    const store = useDiscountCodesStore();
    await store.fetchDiscountCodes();
    mockFetch.mockRejectedValueOnce(new Error('offline'));
    // Act
    const deleted = await store.deleteDiscountCode('SPRING10');
    // Assert
    expect(deleted).toBe(false);
    expect(store.discountCodes).toEqual([springCode]);
  });
});
