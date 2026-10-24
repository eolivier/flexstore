<script setup lang="ts">
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSettingsStore } from '@/stores/settings.store';
  import OrderConfirmationEmailSetting from './OrderConfirmationEmailSetting.vue';

  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  onMounted(() => {
    settingsStore.fetchSettings();
  });
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('settings.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-8">{{ t('settings.subtitle') }}</p>

    <v-alert v-if="settingsStore.loadError" type="error" variant="tonal">{{ t('settings.loadError') }}</v-alert>

    <v-skeleton-loader v-else-if="!settingsStore.settings" type="list-item-avatar-two-line" class="rounded-xl" />

    <v-card v-else class="pa-6">
      <OrderConfirmationEmailSetting />
    </v-card>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
</style>
