<script setup lang="ts">
  import FormActions from '@/components/form/FormActions.vue';
  import { ref, watch } from 'vue';
  import SettingTitle from './SettingTitle.vue';
  import { useI18n } from 'vue-i18n';
  import { useSettingsStore, type ShopSettings } from '@/stores/settings.store';

  const props = defineProps<{ settings: ShopSettings }>();

  // The price of the items from which the standard shipping is free
  const { t } = useI18n();
  const settingsStore = useSettingsStore();

  const amount = ref(props.settings.freeShippingThreshold);
  const valid = ref(true);
  const saving = ref(false);
  // null until saved
  const saved = ref<boolean | null>(null);

  watch(
    () => props.settings.freeShippingThreshold,
    (stored) => {
      amount.value = stored;
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
    <v-avatar color="secondary" variant="tonal" rounded="lg" size="36">
      <v-icon icon="mdi-truck-fast-outline" size="small" />
    </v-avatar>
    <div class="flex-grow-1">
      <SettingTitle>{{ t('settings.freeShippingThreshold') }}</SettingTitle>
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
        <FormActions :cancellable="false" :loading="saving" :disabled="!valid" />
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
