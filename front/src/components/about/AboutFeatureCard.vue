<script setup lang="ts">
  defineProps<{ icon: string; color: string; title: string; text: string }>();
</script>

<template>
  <v-card variant="tonal" :color="color" class="pa-4 h-100">
    <div class="d-flex align-center ga-3 mb-2">
      <v-icon :icon="icon" size="36" />
      <h4 class="text-subtitle-1 font-weight-bold">{{ title }}</h4>
    </div>
    <p class="text-body-2 text-medium-emphasis">{{ text }}</p>
  </v-card>
</template>
