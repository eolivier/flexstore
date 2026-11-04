<script setup lang="ts">
  defineProps<{ icon: string; color: string; title: string; text: string }>();
</script>

<template>
  <v-card variant="tonal" :color="color" class="pa-4 h-100 d-flex ga-4">
    <v-icon :icon="icon" size="36" />
    <div>
      <h4 class="text-subtitle-1 font-weight-bold">{{ title }}</h4>
      <p class="text-body-2 text-medium-emphasis">{{ text }}</p>
    </div>
  </v-card>
</template>
