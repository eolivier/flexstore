<script setup lang="ts">
  import NumberSettingForm from './NumberSettingForm.vue';
  import SettingTitle from './SettingTitle.vue';
  import { useI18n } from 'vue-i18n';
  import { MAX_PAYMENT_DELAY_MINUTES, useSettingsStore, type ShopSettings } from '@/stores/settings.store';

  defineProps<{ settings: ShopSettings }>();

  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  const range = (v: number | string) => {
    const value = Number(v);
    return (
      (Number.isInteger(value) && value >= 1 && value <= MAX_PAYMENT_DELAY_MINUTES) || t('settings.paymentDelayRange')
    );
  };
</script>

<template>
  <div class="d-flex align-start ga-4">
    <div class="flex-grow-1">
      <SettingTitle icon="mdi-timer-sand">{{ t('settings.paymentDelay') }}</SettingTitle>
      <p class="text-body-2 text-medium-emphasis mb-3">{{ t('settings.paymentDelayText') }}</p>
      <NumberSettingForm
        :stored="settings.paymentDelayMinutes"
        :label="t('settings.paymentDelayMinutes')"
        :rule="range"
        :save="settingsStore.changePaymentDelay"
        :min="1"
        :max="MAX_PAYMENT_DELAY_MINUTES"
      />
    </div>
  </div>
</template>
