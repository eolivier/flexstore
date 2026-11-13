<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useGuidedTourStore } from '@/stores/guidedTour.store';

  // Shows or hides the guided tour of the shop: turning it on starts it again
  const { t } = useI18n();
  const guidedTourStore = useGuidedTourStore();
</script>

<template>
  <v-switch
    :model-value="guidedTourStore.isShown"
    :label="t('preferences.guidedTour')"
    :hint="t('preferences.guidedTourHint')"
    persistent-hint
    color="primary"
    @update:model-value="(shown) => guidedTourStore.choose(shown === true)"
  />
</template>
