import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JsonRatingSummary, JsonReview, JsonReviewEligibility } from '@/generated';
import { reviewsApi } from '@/api';
import { useAuthStore } from './auth.store';

// Whether the signed-in customer may review the product being read
export type ReviewEligibility = JsonReviewEligibility['eligibility'];

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
  // null while unknown (a visitor, or not loaded): no form is offered
  const eligibility = ref<ReviewEligibility | null>(null);
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

  // For a signed-in customer: whether they may review the product
  async function fetchEligibility(productId: string) {
    eligibility.value = null;
    const response = await authStore
      .authFetch(`/api/products/${encodeURIComponent(productId)}/review-eligibility`)
      .catch(() => null);
    if (!response?.ok) return;
    const jsonEligibility: JsonReviewEligibility = await response.json();
    eligibility.value = jsonEligibility.eligibility;
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
    eligibility.value = 'ALREADY_REVIEWED';
    await Promise.all([fetchReviews(productId), fetchRatings()]);
    return 'added';
  }

  return { ratings, reviews, eligibility, fetchRatings, ratingOf, fetchReviews, fetchEligibility, addReview };
});
