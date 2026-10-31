<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonReview } from '@/generated';

  // The reviews of a product, newest first
  defineProps<{ reviews: JsonReview[] }>();

  const { t, locale } = useI18n();

  const dateOf = (review: JsonReview) => new Date(review.reviewedAt).toLocaleDateString(locale.value);
</script>

<template>
  <p v-if="reviews.length === 0" class="text-body-2 text-medium-emphasis">{{ t('reviews.none') }}</p>
  <div v-else class="d-flex flex-column ga-4">
    <div v-for="review in reviews" :key="review.id" data-test="review">
      <div class="d-flex align-center flex-wrap ga-2">
        <v-rating :model-value="review.rating" readonly density="compact" size="small" color="warning" />
        <span class="text-body-2 font-weight-bold">{{ review.reviewerName }}</span>
        <span class="text-caption text-medium-emphasis">{{ dateOf(review) }}</span>
      </div>
      <p class="text-body-2 mt-1">{{ review.comment }}</p>
    </div>
  </div>
</template>
