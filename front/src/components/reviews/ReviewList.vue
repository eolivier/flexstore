<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonReview } from '@/generated';

  // The reviews of a product, newest first; deletable: an administrator sees a button to delete each one
  defineProps<{ reviews: JsonReview[]; deletable?: boolean }>();
  defineEmits<{ (e: 'delete', review: JsonReview): void }>();

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
        <v-btn
          v-if="deletable"
          icon="mdi-delete-outline"
          variant="text"
          size="x-small"
          color="error"
          class="ml-auto"
          data-test="delete-review"
          :aria-label="t('reviews.delete')"
          @click="$emit('delete', review)"
        />
      </div>
      <p class="text-body-2 mt-1">{{ review.comment }}</p>
    </div>
  </div>
</template>
