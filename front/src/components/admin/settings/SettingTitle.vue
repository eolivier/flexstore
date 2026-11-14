<script setup lang="ts">
  defineProps<{ icon: string }>();
</script>

<template>
  <div class="d-flex align-center ga-3 mb-1">
    <v-avatar color="secondary" variant="tonal" rounded="lg" size="32">
      <v-icon :icon="icon" size="small" />
    </v-avatar>
    <h2 class="text-body-1 font-weight-bold"><slot /></h2>
  </div>
</template>
