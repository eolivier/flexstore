<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';
  import { useReviewsStore, type ReviewOutcome } from '@/stores/reviews.store';
  import ProductRating from './ProductRating.vue';
  import ReviewForm from './ReviewForm.vue';
  import ReviewList from './ReviewList.vue';

  // The section of the reviews of a product: its rating, the reviews, and the form, offered to the signed-in
  // customers who bought the product and did not review it yet
  const props = defineProps<{ productId: string }>();

  const { t } = useI18n();
  const authStore = useAuthStore();
  const reviewsStore = useReviewsStore();
  const submitting = ref(false);
  const outcome = ref<ReviewOutcome | null>(null);

  const rating = computed(() => reviewsStore.ratingOf(props.productId));
  const signedIn = computed(() => authStore.isAuthenticated);
  const canReview = computed(
    () => reviewsStore.eligibility === 'CAN_REVIEW' && outcome.value !== 'alreadyReviewed'
  );

  watch(
    () => props.productId,
    (productId) => {
      outcome.value = null;
      reviewsStore.fetchReviews(productId);
      reviewsStore.fetchRatings();
    },
    { immediate: true }
  );

  // Asked again when the customer signs in or out on the page
  watch(
    [() => props.productId, signedIn],
    ([productId, isSignedIn]) => {
      if (isSignedIn) reviewsStore.fetchEligibility(productId);
      else reviewsStore.eligibility = null;
    },
    { immediate: true }
  );

  async function submit(review: { rating: number; comment: string }) {
    submitting.value = true;
    outcome.value = await reviewsStore.addReview(props.productId, review.rating, review.comment);
    submitting.value = false;
  }
</script>

<template>
  <section class="mb-12">
    <h2 class="text-h5 font-weight-bold mb-2">{{ t('reviews.title') }}</h2>
    <ProductRating v-if="rating" :rating="rating" size="default" class="mb-4" />
    <ReviewList :reviews="reviewsStore.reviews" class="mb-6" />

    <v-alert v-if="outcome === 'added'" type="success" variant="tonal" density="compact">{{
      t('reviews.thanks')
    }}</v-alert>
    <ReviewForm v-else-if="signedIn && canReview" :submitting="submitting" :outcome="outcome" @submit="submit" />
    <p
      v-else-if="signedIn && reviewsStore.eligibility === 'NOT_BOUGHT'"
      class="text-body-2 text-medium-emphasis"
      data-test="not-bought"
    >
      {{ t('reviews.onlyBuyers') }}
    </p>
    <p
      v-else-if="signedIn && (reviewsStore.eligibility === 'ALREADY_REVIEWED' || outcome === 'alreadyReviewed')"
      class="text-body-2 text-medium-emphasis"
      data-test="already-reviewed"
    >
      {{ t('reviews.errors.alreadyReviewed') }}
    </p>
    <p v-else-if="!signedIn" class="text-body-2 text-medium-emphasis">
      <router-link :to="{ path: '/login', query: { redirect: `/products/${productId}` } }" class="text-secondary">{{ t('reviews.signIn') }}</router-link>
    </p>
  </section>
</template>
