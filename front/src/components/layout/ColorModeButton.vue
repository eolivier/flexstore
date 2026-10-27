<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useColorMode } from '@/composables/useColorMode';

  const { t } = useI18n();
  const { colorMode, switchColorMode } = useColorMode();

  // The button shows the mode it switches to
  const icon = computed(() => (colorMode.value === 'dark' ? 'mdi-white-balance-sunny' : 'mdi-weather-night'));
  const label = computed(() => (colorMode.value === 'dark' ? t('nav.lightMode') : t('nav.darkMode')));
</script>

<template>
  <v-btn :icon="icon" variant="text" :aria-label="label" :title="label" @click="switchColorMode" />
</template>
