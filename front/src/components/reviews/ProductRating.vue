<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { Rating } from '@/stores/reviews.store';

  // The stars of the average rating of a product and the number of its reviews
  defineProps<{ rating: Rating; size?: string }>();

  const { t } = useI18n();
</script>

<template>
  <div class="d-flex align-center ga-1" data-test="product-rating">
    <v-rating
      :model-value="rating.average"
      half-increments
      readonly
      density="compact"
      :size="size ?? 'small'"
      color="warning"
      :aria-label="t('reviews.average', { average: rating.average })"
    />
    <span class="text-caption text-medium-emphasis">
      {{ rating.average.toFixed(1) }} ({{ t('reviews.count', rating.reviewCount) }})
    </span>
  </div>
</template>
