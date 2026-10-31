<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonReview } from '@/generated';
  import ConfirmDialog from '@/components/ConfirmDialog.vue';
  import { useAuthStore } from '@/stores/auth.store';
  import { useReviewsStore, type ReviewOutcome } from '@/stores/reviews.store';
  import ProductRating from './ProductRating.vue';
  import ReviewForm from './ReviewForm.vue';
  import ReviewList from './ReviewList.vue';

  // The section of the reviews of a product: its rating, the reviews, and the form of a signed-in customer
  const props = defineProps<{ productId: string }>();

  const { t } = useI18n();
  const authStore = useAuthStore();
  const reviewsStore = useReviewsStore();
  const submitting = ref(false);
  const outcome = ref<ReviewOutcome | null>(null);
  // The review an administrator is about to delete, once confirmed
  const reviewToDelete = ref<JsonReview | null>(null);
  const deletionFailed = ref(false);

  const rating = computed(() => reviewsStore.ratingOf(props.productId));
  const signedIn = computed(() => authStore.isAuthenticated);
  const reviewed = computed(() => outcome.value === 'added' || outcome.value === 'alreadyReviewed');

  watch(
    () => props.productId,
    (productId) => {
      outcome.value = null;
      reviewsStore.fetchReviews(productId);
      reviewsStore.fetchRatings();
    },
    { immediate: true }
  );

  async function confirmDeletion() {
    if (!reviewToDelete.value) return;
    deletionFailed.value = !(await reviewsStore.deleteReview(reviewToDelete.value.id));
    reviewToDelete.value = null;
  }

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
    <ReviewList
      :reviews="reviewsStore.reviews"
      :deletable="authStore.isAdmin"
      class="mb-6"
      @delete="(review) => (reviewToDelete = review)"
    />
    <v-alert v-if="deletionFailed" type="error" variant="tonal" density="compact" class="mb-4">{{
      t('reviews.deletionFailed')
    }}</v-alert>

    <v-alert v-if="outcome === 'added'" type="success" variant="tonal" density="compact">{{
      t('reviews.thanks')
    }}</v-alert>
    <ReviewForm v-else-if="signedIn && !reviewed" :submitting="submitting" :outcome="outcome" @submit="submit" />
    <p v-else-if="!signedIn" class="text-body-2 text-medium-emphasis">
      <router-link :to="{ path: '/login', query: { redirect: `/products/${productId}` } }" class="text-secondary">{{ t('reviews.signIn') }}</router-link>
    </p>
    <ConfirmDialog
      :model-value="reviewToDelete !== null"
      :title="t('reviews.deleteTitle', { name: reviewToDelete?.reviewerName })"
      :message="t('reviews.deleteMessage')"
      @update:model-value="(open: boolean) => !open && (reviewToDelete = null)"
      @confirm="confirmDeletion"
    />
  </section>
</template>
