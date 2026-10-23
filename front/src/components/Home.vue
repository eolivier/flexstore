<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
</script>

<template>
  <v-container class="py-10">
    <h1 class="text-h4 font-weight-bold mb-4">{{ t('home.title') }}</h1>

    <p class="text-body-1 text-medium-emphasis">{{ t('home.intro') }}</p>
  </v-container>
</template>
