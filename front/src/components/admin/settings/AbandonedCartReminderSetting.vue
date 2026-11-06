<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { MAX_ABANDONED_CART_DELAY_HOURS, useSettingsStore } from '@/stores/settings.store';

  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  const hours = ref(settingsStore.settings?.abandonedCartDelayHours ?? 24);
  const valid = ref(true);
  const saving = ref(false);
  // null until saved
  const saved = ref<boolean | null>(null);

  watch(
    () => settingsStore.settings?.abandonedCartDelayHours,
    (stored) => {
      if (stored) hours.value = stored;
    }
  );

  const rules = {
    range: (v: number | string) => {
      const value = Number(v);
      return (
        (Number.isInteger(value) && value >= 1 && value <= MAX_ABANDONED_CART_DELAY_HOURS) ||
        t('settings.abandonedCartDelayRange')
      );
    },
  };

  // The switch gives the wished state: it is turned into the matching intent right away
  async function switchReminder(wished: boolean | null) {
    saving.value = true;
    saved.value = wished
      ? await settingsStore.enableAbandonedCartReminder()
      : await settingsStore.disableAbandonedCartReminder();
    saving.value = false;
  }

  async function saveDelay() {
    if (!valid.value) return;
    saving.value = true;
    saved.value = await settingsStore.changeAbandonedCartDelay(Number(hours.value));
    saving.value = false;
  }
</script>

<template>
  <div class="d-flex align-start ga-4">
    <v-avatar color="secondary" variant="tonal" rounded="lg" size="44">
      <v-icon icon="mdi-cart-arrow-right" />
    </v-avatar>
    <div class="flex-grow-1">
      <h2 class="text-subtitle-1 font-weight-bold">{{ t('settings.abandonedCartReminder') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-3">{{ t('settings.abandonedCartReminderText') }}</p>
      <v-form v-model="valid" class="d-flex align-start ga-3" @submit.prevent="saveDelay">
        <v-text-field
          v-model="hours"
          type="number"
          min="1"
          :max="MAX_ABANDONED_CART_DELAY_HOURS"
          :label="t('settings.abandonedCartDelayHours')"
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
    <v-switch
      :model-value="settingsStore.settings?.abandonedCartReminder === 'ENABLED'"
      :disabled="saving"
      :aria-label="t('settings.abandonedCartReminder')"
      color="secondary"
      inset
      hide-details
      @update:model-value="switchReminder"
    />
  </div>
</template>

<style scoped>
  .delay-field {
    max-width: 180px;
  }
</style>
