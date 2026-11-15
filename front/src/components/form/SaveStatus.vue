<script setup lang="ts">
  import type { SaveOutcome } from './saveOutcome';

  // What a save of an edit form answered, next to its buttons: the same words and colours in every form
  defineProps<{ outcome: SaveOutcome | null; saved: string; failed: string; invalid?: string }>();
</script>

<template>
  <span v-if="outcome === 'saved'" class="text-body-2 text-success" data-test="save-status">{{ saved }}</span>
  <span v-else-if="outcome === 'invalid'" class="text-body-2 text-error" data-test="save-status">{{ invalid }}</span>
  <span v-else-if="outcome === 'failed'" class="text-body-2 text-error" data-test="save-status">{{ failed }}</span>
</template>
