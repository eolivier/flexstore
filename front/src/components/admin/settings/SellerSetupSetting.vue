<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useSettingsStore } from '@/stores/settings.store';
  import { SELLER_SETUP_PATH } from '@/utils/sellerSetupRedirect';

  const { t } = useI18n();
  const settingsStore = useSettingsStore();
</script>

<template>
  <div class="d-flex align-start ga-4">
    <v-avatar color="secondary" variant="tonal" rounded="lg" size="36">
      <v-icon icon="mdi-store-cog-outline" size="small" />
    </v-avatar>
    <div class="flex-grow-1">
      <h2 class="text-subtitle-1 font-weight-bold">{{ t('settings.sellerSetup') }}</h2>
      <p class="text-body-2 text-medium-emphasis">{{ t('settings.sellerSetupText') }}</p>
      <router-link v-if="settingsStore.settings?.sellerSetup === 'DISABLED'" :to="SELLER_SETUP_PATH">{{
        t('settings.sellerSetupOpen')
      }}</router-link>
    </div>
    <!-- Turned on by the setup assistant of the seller only: shown, never changed from here -->
    <v-switch
      :model-value="settingsStore.settings?.sellerSetup === 'ENABLED'"
      :aria-label="t('settings.sellerSetup')"
      color="secondary"
      inset
      hide-details
      disabled
      readonly
    />
  </div>
</template>
