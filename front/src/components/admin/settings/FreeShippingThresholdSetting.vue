<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSettingsStore } from '@/stores/settings.store';

  // The price of the items from which the standard shipping is free
  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  const amount = ref(settingsStore.settings?.freeShippingThreshold ?? 50);
  const valid = ref(true);
  const saving = ref(false);
  // null until saved
  const saved = ref<boolean | null>(null);

  watch(
    () => settingsStore.settings?.freeShippingThreshold,
    (stored) => {
      if (stored !== undefined) amount.value = stored;
    }
  );

  const positiveOrZero = (v: number | string) => Number(v) >= 0 || t('settings.freeShippingThresholdRange');

  async function save() {
    if (!valid.value) return;
    saving.value = true;
    saved.value = await settingsStore.changeFreeShippingThreshold(Number(amount.value));
    saving.value = false;
  }
</script>

<template>
  <div class="d-flex align-start ga-4">
    <v-avatar color="secondary" variant="tonal" rounded="lg" size="44">
      <v-icon icon="mdi-truck-fast-outline" />
    </v-avatar>
    <div class="flex-grow-1">
      <h2 class="text-subtitle-1 font-weight-bold">{{ t('settings.freeShippingThreshold') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-3">{{ t('settings.freeShippingThresholdText') }}</p>
      <v-form v-model="valid" class="d-flex align-start ga-3" @submit.prevent="save">
        <v-text-field
          v-model="amount"
          type="number"
          min="0"
          step="0.01"
          :label="t('settings.freeShippingThresholdAmount')"
          :rules="[positiveOrZero]"
          density="compact"
          class="amount-field"
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
  .amount-field {
    max-width: 180px;
  }
</style>
