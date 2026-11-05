<template>
  <v-menu location="bottom end">
    <template #activator="{ props }">
      <v-btn v-bind="props" variant="text" :aria-label="t('nav.language')" prepend-icon="mdi-translate">
        {{ locale.toUpperCase() }}
      </v-btn>
    </template>
    <v-list density="compact" min-width="160">
      <v-list-item
        v-for="language in languages"
        :key="language.value"
        :active="locale === language.value"
        color="secondary"
        @click="changeLocale(language.value)"
      >
        <v-list-item-title>{{ language.title }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup>
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter } from 'vue-router';
  import { isTranslatedPath, localizedPath } from '@/utils/language';

  const { locale, t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const languages = [
    { title: 'Français', value: 'fr' },
    { title: 'English', value: 'en' },
  ];

  // A page that exists in each language moves to the address of the new one
  function changeLocale(val) {
    locale.value = val;
    if (isTranslatedPath(route.path)) router.push({ path: localizedPath(route.path, val), query: route.query });
  }
</script>
