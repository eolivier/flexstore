<script setup lang="ts">
  // A message about what happened (a refusal, a failure, a state, a confirmation), the same way everywhere
  withDefaults(defineProps<{ type: 'error' | 'warning' | 'info' | 'success'; compact?: boolean }>(), {
    compact: false,
  });
</script>

<template>
  <v-alert :type="type" variant="tonal" :density="compact ? 'compact' : 'default'">
    <slot />
    <template v-if="$slots.append" #append><slot name="append" /></template>
  </v-alert>
</template>
