<script setup lang="ts">
  import LoadError from '@/components/layout/LoadError.vue';
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSettingsStore } from '@/stores/settings.store';
  import SellerSetupSetting from './SellerSetupSetting.vue';
  import OrderConfirmationEmailSetting from './OrderConfirmationEmailSetting.vue';
  import AbandonedCartReminderSetting from './AbandonedCartReminderSetting.vue';
  import PaymentDelaySetting from './PaymentDelaySetting.vue';
  import FreeShippingThresholdSetting from './FreeShippingThresholdSetting.vue';

  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  onMounted(() => {
    settingsStore.fetchSettings();
  });
</script>

<template>
  <v-container class="py-6">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('settings.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t('settings.subtitle') }}</p>

    <LoadError v-if="settingsStore.load.status === 'failed'" :message="t('settings.loadError')" />

    <v-skeleton-loader
      v-else-if="settingsStore.load.status === 'loading'"
      type="list-item-avatar-two-line"
      class="rounded-xl"
    />

    <v-card v-else class="pa-4">
      <SellerSetupSetting :settings="settingsStore.load.settings" />
      <v-divider class="my-3" />
      <OrderConfirmationEmailSetting :settings="settingsStore.load.settings" />
      <v-divider class="my-3" />
      <AbandonedCartReminderSetting :settings="settingsStore.load.settings" />
      <v-divider class="my-3" />
      <PaymentDelaySetting :settings="settingsStore.load.settings" />
      <v-divider class="my-3" />
      <FreeShippingThresholdSetting :settings="settingsStore.load.settings" />
    </v-card>
  </v-container>
</template>
