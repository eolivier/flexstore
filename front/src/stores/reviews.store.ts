import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonRatingSummary, JsonReview } from '@/generated';
import { reviewsApi } from '@/api';
import { useAuthStore } from './auth.store';

// How the sending of a review ended: notBought (403), alreadyReviewed (409), invalid (400)
export type ReviewOutcome = 'added' | 'notBought' | 'alreadyReviewed' | 'invalid' | 'error';

export interface Rating {
  average: number;
  reviewCount: number;
}

const OUTCOME_OF_STATUS: Record<number, ReviewOutcome> = {
  400: 'invalid',
  403: 'notBought',
  409: 'alreadyReviewed',
};

// What the customers think of the products, and the reviews of the product being read
export const useReviewsStore = defineStore('reviews', () => {
  const ratings = ref<Record<string, Rating>>({});
  const reviews = ref<JsonReview[]>([]);
  const authStore = useAuthStore();

  async function fetchRatings() {
    const response = await reviewsApi.getRatings().catch(() => null);
    if (!response) return;
    const summaries: JsonRatingSummary[] = response.data;
    ratings.value = Object.fromEntries(
      summaries.map(({ productId, average, reviewCount }) => [productId, { average, reviewCount }])
    );
  }

  // The rating of a product, or null when nobody reviewed it
  function ratingOf(productId: string | undefined): Rating | null {
    return productId === undefined ? null : (ratings.value[productId] ?? null);
  }

  async function fetchReviews(productId: string) {
    const response = await reviewsApi.getReviews(productId).catch(() => null);
    reviews.value = response ? response.data : [];
  }

  // For a signed-in customer who bought the product; the reviews and the ratings are read again once added
  async function addReview(productId: string, rating: number, comment: string): Promise<ReviewOutcome> {
    const response = await authStore
      .authFetch(`/api/products/${encodeURIComponent(productId)}/reviews`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rating, comment }),
      })
      .catch(() => null);
    if (!response) return 'error';
    if (!response.ok) return OUTCOME_OF_STATUS[response.status] ?? 'error';
    await Promise.all([fetchReviews(productId), fetchRatings()]);
    return 'added';
  }

  // For the administrators; returns whether the review was deleted. The ratings are read again
  async function deleteReview(reviewId: string): Promise<boolean> {
    const response = await authStore
      .authFetch(`/api/admin/reviews/${encodeURIComponent(reviewId)}`, { method: 'DELETE' })
      .catch(() => null);
    if (!response?.ok) return false;
    reviews.value = reviews.value.filter((review) => review.id !== reviewId);
    await fetchRatings();
    return true;
  }

  return { ratings, reviews, fetchRatings, ratingOf, fetchReviews, addReview, deleteReview };
});
