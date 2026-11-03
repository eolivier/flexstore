<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useCookieConsentStore } from '@/stores/cookieConsent.store';

  const { t } = useI18n();
  const cookieConsentStore = useCookieConsentStore();
</script>

<template>
  <v-card
    v-if="cookieConsentStore.isUndecided"
    class="cookie-banner pa-4"
    elevation="8"
    role="region"
    :aria-label="t('cookies.title')"
  >
    <h2 class="text-subtitle-1 font-weight-bold mb-1">{{ t('cookies.title') }}</h2>
    <p class="text-body-2 mb-3">{{ t('cookies.bannerText') }}</p>
    <div class="d-flex flex-wrap ga-2 justify-end">
      <v-btn variant="outlined" @click="cookieConsentStore.choose('refused')">{{ t('cookies.refuse') }}</v-btn>
      <v-btn color="primary" @click="cookieConsentStore.choose('accepted')">{{ t('cookies.accept') }}</v-btn>
    </div>
  </v-card>
</template>

<style scoped>
  .cookie-banner {
    position: fixed;
    right: 16px;
    bottom: 16px;
    left: 16px;
    z-index: 2000;
    max-width: 480px;
    margin-left: auto;
  }
</style>
