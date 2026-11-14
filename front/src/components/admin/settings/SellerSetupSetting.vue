<script setup lang="ts">
  import SettingTitle from './SettingTitle.vue';
  import { useI18n } from 'vue-i18n';
  import type { ShopSettings } from '@/stores/settings.store';
  import { SELLER_SETUP_PATH } from '@/utils/sellerSetupRedirect';

  const { t } = useI18n();
  defineProps<{ settings: ShopSettings }>();
</script>

<template>
  <div class="d-flex align-start ga-4">
    <div class="flex-grow-1">
      <SettingTitle icon="mdi-store-cog-outline">{{ t('settings.sellerSetup') }}</SettingTitle>
      <p class="text-body-2 text-medium-emphasis">{{ t('settings.sellerSetupText') }}</p>
      <router-link v-if="settings.sellerSetup === 'DISABLED'" :to="SELLER_SETUP_PATH">{{
        t('settings.sellerSetupOpen')
      }}</router-link>
    </div>
    <!-- Turned on by the setup assistant of the seller only: shown, never changed from here -->
    <v-switch
      :model-value="settings.sellerSetup === 'ENABLED'"
      :aria-label="t('settings.sellerSetup')"
      color="secondary"
      inset
      hide-details
      disabled
      readonly
    />
  </div>
</template>
