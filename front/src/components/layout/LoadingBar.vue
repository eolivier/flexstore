<script setup lang="ts">
  import { onBeforeUnmount, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useLoadingStore } from '@/stores/loading.store';

  // Shown only when a load lasts a little, so that fast requests do not make the bar flicker
  const SHOW_DELAY_MS = 150;

  const { t } = useI18n();
  const loadingStore = useLoadingStore();
  const visible = ref(false);
  let showTimer: ReturnType<typeof setTimeout> | undefined;

  watch(
    () => loadingStore.isLoading,
    (loading) => {
      clearTimeout(showTimer);
      if (loading) {
        showTimer = setTimeout(() => (visible.value = true), SHOW_DELAY_MS);
      } else {
        visible.value = false;
      }
    },
    { immediate: true }
  );

  onBeforeUnmount(() => clearTimeout(showTimer));
</script>

<template>
  <v-progress-linear
    :active="visible"
    indeterminate
    color="secondary"
    height="3"
    absolute
    location="bottom"
    role="progressbar"
    :aria-label="t('loading.inProgress')"
    :aria-busy="visible"
  />
</template>
