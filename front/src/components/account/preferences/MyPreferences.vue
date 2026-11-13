<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useColorMode } from '@/composables/useColorMode';
  import { useLanguage } from '@/composables/useLanguage';
  import { LANGUAGE_NAMES, PAGE_LANGUAGES } from '@/utils/language';
  import AccountBreadcrumbs from '@/components/account/AccountBreadcrumbs.vue';
  import CookieSettings from '@/components/cookies/CookieSettings.vue';
  import GuidedTourSwitch from './GuidedTourSwitch.vue';

  // The preferences of the signed-in customer: they see the display mode and the language they chose and can
  // change them here
  const { t } = useI18n();
  const { colorMode, switchColorMode } = useColorMode();

  const { language, changeLanguage } = useLanguage();
  const languages = PAGE_LANGUAGES.map((value) => ({ title: LANGUAGE_NAMES[value], value }));

  const darkModeChosen = computed(() => colorMode.value === 'dark');
</script>

<template>
  <v-container class="py-10">
    <AccountBreadcrumbs />
    <h1 class="text-h4 font-weight-bold">{{ t('preferences.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mt-1 mb-6">{{ t('preferences.subtitle') }}</p>

    <v-card class="pa-4">
      <v-select
        :model-value="language"
        :items="languages"
        :label="t('preferences.language')"
        :hint="t('preferences.languageHint')"
        persistent-hint
        class="mb-4"
        @update:model-value="changeLanguage"
      />
      <v-switch
        :model-value="darkModeChosen"
        :label="t('preferences.darkMode')"
        :hint="t('preferences.darkModeHint')"
        persistent-hint
        color="primary"
        class="mb-4"
        @update:model-value="switchColorMode"
      />
      <GuidedTourSwitch />
    </v-card>

    <CookieSettings class="mt-6" />
  </v-container>
</template>
