<script setup lang="ts">
  import NumberSettingForm from './NumberSettingForm.vue';
  import { ref } from 'vue';
  import SettingTitle from './SettingTitle.vue';
  import { useI18n } from 'vue-i18n';
  import { MAX_ABANDONED_CART_DELAY_HOURS, useSettingsStore, type ShopSettings } from '@/stores/settings.store';

  defineProps<{ settings: ShopSettings }>();

  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  const switching = ref(false);
  // null until the switch is used
  const switched = ref<boolean | null>(null);

  const range = (v: number | string) => {
    const value = Number(v);
    return (
      (Number.isInteger(value) && value >= 1 && value <= MAX_ABANDONED_CART_DELAY_HOURS) ||
      t('settings.abandonedCartDelayRange')
    );
  };

  // The switch gives the wished state: it is turned into the matching intent right away
  async function switchReminder(wished: boolean | null) {
    switching.value = true;
    switched.value = wished
      ? await settingsStore.enableAbandonedCartReminder()
      : await settingsStore.disableAbandonedCartReminder();
    switching.value = false;
  }
</script>

<template>
  <div class="d-flex align-start ga-4">
    <div class="flex-grow-1">
      <SettingTitle icon="mdi-cart-arrow-right">{{ t('settings.abandonedCartReminder') }}</SettingTitle>
      <p class="text-body-2 text-medium-emphasis mb-3">{{ t('settings.abandonedCartReminderText') }}</p>
      <NumberSettingForm
        :stored="settings.abandonedCartDelayHours"
        :label="t('settings.abandonedCartDelayHours')"
        :rule="range"
        :save="settingsStore.changeAbandonedCartDelay"
        :min="1"
        :max="MAX_ABANDONED_CART_DELAY_HOURS"
      />
      <p v-if="switched === false" class="text-body-2 text-error">{{ t('settings.saveError') }}</p>
      <p v-else-if="switched" class="text-body-2 text-success">{{ t('settings.saved') }}</p>
    </div>
    <v-switch
      :model-value="settings.abandonedCartReminder === 'ENABLED'"
      :disabled="switching"
      :aria-label="t('settings.abandonedCartReminder')"
      color="secondary"
      inset
      hide-details
      @update:model-value="switchReminder"
    />
  </div>
</template>
