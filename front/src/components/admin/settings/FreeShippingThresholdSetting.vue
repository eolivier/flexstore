<script setup lang="ts">
  import NumberSettingForm from './NumberSettingForm.vue';
  import SettingTitle from './SettingTitle.vue';
  import { useI18n } from 'vue-i18n';
  import { useSettingsStore, type ShopSettings } from '@/stores/settings.store';

  defineProps<{ settings: ShopSettings }>();

  // The price of the items from which the standard shipping is free
  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  const positiveOrZero = (v: number | string) => Number(v) >= 0 || t('settings.freeShippingThresholdRange');
</script>

<template>
  <div class="d-flex align-start ga-4">
    <div class="flex-grow-1">
      <SettingTitle icon="mdi-truck-fast-outline">{{ t('settings.freeShippingThreshold') }}</SettingTitle>
      <p class="text-body-2 text-medium-emphasis mb-3">{{ t('settings.freeShippingThresholdText') }}</p>
      <NumberSettingForm
        :stored="settings.freeShippingThreshold"
        :label="t('settings.freeShippingThresholdAmount')"
        :rule="positiveOrZero"
        :save="settingsStore.changeFreeShippingThreshold"
        :min="0"
        :step="0.01"
      />
    </div>
  </div>
</template>
