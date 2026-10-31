import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useReviewsStore } from './reviews.store';

const getRatings = vi.fn();
const getReviews = vi.fn();
vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  reviewsApi: { getRatings: () => getRatings(), getReviews: (id: string) => getReviews(id) },
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('reviews.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    getRatings.mockResolvedValue({ data: [{ productId: 'p-1', average: 4.3, reviewCount: 3 }] });
    getReviews.mockResolvedValue({ data: [] });
  });

  it('knows the rating of a reviewed product only', async () => {
    // Arrange
    const store = useReviewsStore();
    // Act
    await store.fetchRatings();
    // Assert
    expect(store.ratingOf('p-1')).toEqual({ average: 4.3, reviewCount: 3 });
    expect(store.ratingOf('p-2')).toBeNull();
  });

  it('reads the reviews and the ratings again once a review is added', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 201 });
    const store = useReviewsStore();
    // Act
    const outcome = await store.addReview('p-1', 5, 'Sturdy');
    // Assert
    expect(outcome).toBe('added');
    expect(getReviews).toHaveBeenCalledWith('p-1');
    expect(store.ratingOf('p-1')?.reviewCount).toBe(3);
  });

  it.each([
    { status: 400, expected: 'invalid' },
    { status: 403, expected: 'notBought' },
    { status: 409, expected: 'alreadyReviewed' },
    { status: 500, expected: 'error' },
  ])('a $status answer reads $expected', async ({ status, expected }) => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status });
    const store = useReviewsStore();
    // Act
    const outcome = await store.addReview('p-1', 5, 'Sturdy');
    // Assert
    expect(outcome).toBe(expected);
  });

  it.each(['CAN_REVIEW', 'NOT_BOUGHT', 'ALREADY_REVIEWED'])(
    'knows whether the customer may review the product: %s',
    async (eligibility) => {
      // Arrange
      mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({ eligibility }) });
      const store = useReviewsStore();
      // Act
      await store.fetchEligibility('p-1');
      // Assert
      expect(store.eligibility).toBe(eligibility);
    }
  );

  it('offers no form when the eligibility could not be read', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 401 });
    const store = useReviewsStore();
    // Act
    await store.fetchEligibility('p-1');
    // Assert
    expect(store.eligibility).toBeNull();
  });

  it('knows the product reviewed once the review is added', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 201 });
    const store = useReviewsStore();
    // Act
    await store.addReview('p-1', 5, 'Sturdy');
    // Assert
    expect(store.eligibility).toBe('ALREADY_REVIEWED');
  });
});
