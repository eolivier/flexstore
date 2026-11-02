<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // The units at or under which the administrators are warned that the product runs low, 0 or more
  const threshold = defineModel<string>({ required: true });

  const { t } = useI18n();

  const thresholdRule = (v: string) => /^\d+$/.test(v) || t('stockAlerts.invalidThreshold');
</script>

<template>
  <v-text-field
    v-model="threshold"
    :label="t('stockAlerts.threshold')"
    :hint="t('stockAlerts.thresholdHint')"
    type="number"
    min="0"
    :rules="[thresholdRule]"
    prepend-inner-icon="mdi-bell-alert-outline"
    class="mt-2"
  />
</template>
