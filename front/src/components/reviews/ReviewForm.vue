<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { ReviewOutcome } from '@/stores/reviews.store';

  // The form of a customer who reviews a product: a rating and a comment; outcome is how the last sending ended
  const props = defineProps<{ submitting?: boolean; outcome?: ReviewOutcome | null }>();
  const emit = defineEmits<{ (e: 'submit', review: { rating: number; comment: string }): void }>();

  const { t } = useI18n();
  const rating = ref(0);
  const comment = ref('');

  const canSubmit = computed(() => rating.value >= 1 && comment.value.trim().length > 0);
  const errorKey = computed(() =>
    props.outcome && props.outcome !== 'added' ? `reviews.errors.${props.outcome}` : null
  );

  function submit() {
    if (canSubmit.value) emit('submit', { rating: rating.value, comment: comment.value.trim() });
  }
</script>

<template>
  <v-form class="d-flex flex-column ga-3" @submit.prevent="submit">
    <div class="text-subtitle-1 font-weight-bold">{{ t('reviews.write') }}</div>
    <v-rating v-model="rating" hover color="warning" :aria-label="t('reviews.yourRating')" />
    <v-textarea
      v-model="comment"
      :label="t('reviews.comment')"
      counter="1000"
      maxlength="1000"
      rows="3"
      auto-grow
      variant="outlined"
    />
    <v-alert v-if="errorKey" type="error" variant="tonal" density="compact">{{ t(errorKey) }}</v-alert>
    <v-btn type="submit" color="primary" class="align-self-start" :loading="submitting" :disabled="!canSubmit">
      {{ t('reviews.publish') }}
    </v-btn>
  </v-form>
</template>
