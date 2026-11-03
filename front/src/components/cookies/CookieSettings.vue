<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useCookieConsentStore } from '@/stores/cookieConsent.store';

  const { t } = useI18n();
  const cookieConsentStore = useCookieConsentStore();

  function choosePreferences(accepted: boolean | null) {
    cookieConsentStore.choose(accepted ? 'accepted' : 'refused');
  }
</script>

<template>
  <v-card class="pa-6">
    <h2 class="text-h6 font-weight-bold mb-1">{{ t('cookies.settingsTitle') }}</h2>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t('cookies.settingsText') }}</p>
    <v-switch :model-value="true" disabled hide-details color="primary" :label="t('cookies.essential')" />
    <p class="text-caption text-medium-emphasis mb-3">{{ t('cookies.essentialText') }}</p>
    <v-switch
      :model-value="cookieConsentStore.preferencesAccepted"
      hide-details
      color="primary"
      :label="t('cookies.preferences')"
      @update:model-value="choosePreferences"
    />
    <p class="text-caption text-medium-emphasis">{{ t('cookies.preferencesText') }}</p>
  </v-card>
</template>
