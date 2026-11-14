<script setup lang="ts">
  import { ref } from 'vue';
  import SettingTitle from './SettingTitle.vue';
  import { useI18n } from 'vue-i18n';
  import { useSettingsStore, type ShopSettings } from '@/stores/settings.store';

  const { t } = useI18n();
  defineProps<{ settings: ShopSettings }>();
  const settingsStore = useSettingsStore();

  const saving = ref(false);
  const saveError = ref(false);

  // The switch gives the wished state: it is turned into the matching intent right away
  async function switchOrderConfirmationEmail(wished: boolean | null) {
    saving.value = true;
    const saved = wished
      ? await settingsStore.enableOrderConfirmationEmail()
      : await settingsStore.disableOrderConfirmationEmail();
    saveError.value = !saved;
    saving.value = false;
  }
</script>

<template>
  <div class="d-flex align-start ga-4">
    <div class="flex-grow-1">
      <SettingTitle icon="mdi-email-check-outline">{{ t('settings.orderConfirmationEmail') }}</SettingTitle>
      <p class="text-body-2 text-medium-emphasis">{{ t('settings.orderConfirmationEmailText') }}</p>
      <p v-if="saveError" class="text-body-2 text-error mt-1">{{ t('settings.saveError') }}</p>
    </div>
    <v-switch
      :model-value="settings.orderConfirmationEmail === 'ENABLED'"
      :loading="saving"
      :disabled="saving"
      :aria-label="t('settings.orderConfirmationEmail')"
      color="secondary"
      inset
      hide-details
      @update:model-value="switchOrderConfirmationEmail"
    />
  </div>
</template>
