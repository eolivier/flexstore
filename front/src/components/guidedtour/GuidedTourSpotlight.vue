<script setup lang="ts">
  import { computed } from 'vue';
  import type { FocusArea } from '@/utils/guidedTourFocus';

  // The focus of the guided tour: the page is darkened but the part presented, which stays clear and outlined.
  // The whole page is darkened when no part is presented
  const props = defineProps<{ area: FocusArea | null }>();
  const margin = 6;

  const holeStyle = computed(() => ({
    top: `${(props.area?.top ?? 0) - margin}px`,
    left: `${(props.area?.left ?? 0) - margin}px`,
    width: `${(props.area?.width ?? 0) + 2 * margin}px`,
    height: `${(props.area?.height ?? 0) + 2 * margin}px`,
  }));
</script>

<template>
  <div class="spotlight" :class="{ dark: !area }" aria-hidden="true" data-testid="guided-tour-spotlight">
    <div v-if="area" class="hole" :style="holeStyle" data-testid="guided-tour-hole" />
  </div>
</template>

<style scoped>
  .spotlight {
    position: fixed;
    inset: 0;
    z-index: 2300;
  }
  .dark {
    background: rgba(0, 0, 0, 0.6);
  }
  .hole {
    position: fixed;
    border-radius: 8px;
    outline: 3px solid rgb(var(--v-theme-secondary));
    box-shadow: 0 0 0 100vmax rgba(0, 0, 0, 0.6);
    transition: all 0.25s ease-in-out;
  }
  @media (prefers-reduced-motion: reduce) {
    .hole {
      transition: none;
    }
  }
</style>
