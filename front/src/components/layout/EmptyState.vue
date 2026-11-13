<script setup lang="ts">
  withDefaults(defineProps<{ message: string; description?: string; icon?: string; compact?: boolean }>(), {
    description: undefined,
    icon: undefined,
    compact: false,
  });
</script>

<template>
  <p v-if="compact" class="text-body-2 text-medium-emphasis">{{ message }}</p>
  <v-card v-else class="pa-10 text-center">
    <v-icon v-if="icon" :icon="icon" size="56" class="text-medium-emphasis mb-3" />
    <h2 class="text-h6 font-weight-bold mb-1">{{ message }}</h2>
    <p v-if="description" class="text-body-2 text-medium-emphasis mb-6">{{ description }}</p>
    <div v-if="$slots.default" class="mt-4"><slot /></div>
  </v-card>
</template>
