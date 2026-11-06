<template>
  <v-menu location="bottom end">
    <template #activator="{ props }">
      <v-btn v-bind="props" variant="text" :aria-label="t('nav.language')" prepend-icon="mdi-translate">
        {{ language.toUpperCase() }}
      </v-btn>
    </template>
    <v-list density="compact" min-width="160">
      <v-list-item
        v-for="item in languages"
        :key="item.value"
        :active="language === item.value"
        color="secondary"
        @click="changeLanguage(item.value)"
      >
        <v-list-item-title>{{ item.title }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useLanguage } from '@/composables/useLanguage';
  import { LANGUAGE_NAMES, PAGE_LANGUAGES } from '@/utils/language';

  const { t } = useI18n();
  const { language, changeLanguage } = useLanguage();
  const languages = PAGE_LANGUAGES.map((value) => ({ title: LANGUAGE_NAMES[value], value }));
</script>
