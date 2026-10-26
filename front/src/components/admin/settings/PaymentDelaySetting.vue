<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { MAX_PAYMENT_DELAY_MINUTES, useSettingsStore } from '@/stores/settings.store';

  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  const minutes = ref(settingsStore.settings?.paymentDelayMinutes ?? 10);
  const valid = ref(true);
  const saving = ref(false);
  // null until saved
  const saved = ref<boolean | null>(null);

  watch(
    () => settingsStore.settings?.paymentDelayMinutes,
    (stored) => {
      if (stored) minutes.value = stored;
    }
  );

  const rules = {
    range: (v: number | string) => {
      const value = Number(v);
      return (
        (Number.isInteger(value) && value >= 1 && value <= MAX_PAYMENT_DELAY_MINUTES) ||
        t('settings.paymentDelayRange')
      );
    },
  };

  async function save() {
    if (!valid.value) return;
    saving.value = true;
    saved.value = await settingsStore.changePaymentDelay(Number(minutes.value));
    saving.value = false;
  }
</script>

<template>
  <div class="d-flex align-start ga-4">
    <v-avatar color="secondary" variant="tonal" rounded="lg" size="44">
      <v-icon icon="mdi-timer-sand" />
    </v-avatar>
    <div class="flex-grow-1">
      <h2 class="text-subtitle-1 font-weight-bold">{{ t('settings.paymentDelay') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-3">{{ t('settings.paymentDelayText') }}</p>
      <v-form v-model="valid" class="d-flex align-start ga-3" @submit.prevent="save">
        <v-text-field
          v-model="minutes"
          type="number"
          min="1"
          :max="MAX_PAYMENT_DELAY_MINUTES"
          :label="t('settings.paymentDelayMinutes')"
          :rules="[rules.range]"
          density="compact"
          class="delay-field"
        />
        <v-btn type="submit" color="secondary" :loading="saving" :disabled="!valid">{{
          t('settings.save')
        }}</v-btn>
      </v-form>
      <p v-if="saved === false" class="text-body-2 text-error">{{ t('settings.saveError') }}</p>
      <p v-else-if="saved" class="text-body-2 text-success">{{ t('settings.saved') }}</p>
    </div>
  </div>
</template>

<style scoped>
  .delay-field {
    max-width: 180px;
  }
</style>
