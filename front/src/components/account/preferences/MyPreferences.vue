<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useColorMode } from '@/composables/useColorMode';
  import AccountBreadcrumbs from '@/components/account/AccountBreadcrumbs.vue';

  // The preferences of the signed-in customer: they see the display mode they chose and can change it here
  const { t } = useI18n();
  const { colorMode, switchColorMode } = useColorMode();

  const darkModeChosen = computed(() => colorMode.value === 'dark');
</script>

<template>
  <v-container class="py-10">
    <AccountBreadcrumbs />
    <h1 class="text-h4 font-weight-bold">{{ t('preferences.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mt-1 mb-6">{{ t('preferences.subtitle') }}</p>

    <v-card class="pa-4">
      <v-switch
        :model-value="darkModeChosen"
        :label="t('preferences.darkMode')"
        :hint="t('preferences.darkModeHint')"
        persistent-hint
        color="primary"
        @update:model-value="switchColorMode"
      />
    </v-card>
  </v-container>
</template>
