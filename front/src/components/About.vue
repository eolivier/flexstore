<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();

  const stack = [
    { key: 'backend', icon: 'mdi-language-kotlin' },
    { key: 'database', icon: 'mdi-database-outline' },
    { key: 'frontend', icon: 'mdi-vuejs' },
  ];
</script>

<template>
  <v-container class="about py-12">
    <h1 class="text-h4 font-weight-bold mb-4">{{ t('about.title') }}</h1>
    <p class="text-body-1 text-medium-emphasis mb-10">{{ t('about.intro') }}</p>

    <h2 class="text-h6 font-weight-bold mb-2">{{ t('about.projectTitle') }}</h2>
    <p class="text-body-1 text-medium-emphasis mb-10">{{ t('about.projectText') }}</p>

    <h2 class="text-h6 font-weight-bold mb-4">{{ t('about.stackTitle') }}</h2>
    <div class="d-flex flex-wrap ga-2 mb-10">
      <v-chip v-for="item in stack" :key="item.key" :prepend-icon="item.icon" size="large" variant="outlined">
        {{ t(`about.stack.${item.key}`) }}
      </v-chip>
    </div>

    <v-card class="pa-6 d-flex flex-column flex-sm-row align-sm-center ga-4">
      <div>
        <h2 class="text-h6 font-weight-bold">{{ t('about.sourceTitle') }}</h2>
        <p class="text-body-2 text-medium-emphasis">{{ t('about.sourceText') }}</p>
      </div>
      <v-spacer />
      <v-btn
        href="https://github.com/eolivier/flexstore"
        target="_blank"
        rel="noopener"
        color="primary"
        prepend-icon="mdi-github"
      >
        {{ t('about.sourceButton') }}
      </v-btn>
    </v-card>
  </v-container>
</template>

<style scoped>
  .about {
    max-width: 760px;
  }
</style>
