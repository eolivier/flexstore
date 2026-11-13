<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { GuidedTourSection } from '@/utils/guidedTour';

  // One section of the shop, presented by the guided tour
  defineProps<{ section: GuidedTourSection }>();
  const { t } = useI18n();
</script>

<template>
  <div class="text-center py-4">
    <v-icon :icon="section.icon" size="56" color="primary" class="mb-4" />
    <h2 class="text-h6 font-weight-bold mb-2">{{ t(`${section.messageKey}.title`) }}</h2>
    <p class="text-body-2 text-medium-emphasis">{{ t(`${section.messageKey}.text`) }}</p>
  </div>
</template>
