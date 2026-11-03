<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonOrderTracking } from '@/generated';

  // Where the parcel of a shipped order is followed: its carrier, its tracking number, and the tracking page
  // of the carrier when it has one
  defineProps<{ tracking: JsonOrderTracking }>();

  const { t } = useI18n();
</script>

<template>
  <v-card class="pa-5 mb-6 d-flex flex-wrap align-center ga-4" data-test="order-tracking">
    <v-icon icon="mdi-truck-delivery-outline" color="primary" size="32" />
    <div class="flex-grow-1">
      <div class="text-subtitle-1 font-weight-bold">{{ t('orderTracking.title') }}</div>
      <div class="text-body-2">
        {{ t('orderTracking.carrier') }} : {{ t(`orderTracking.carriers.${tracking.carrier}`) }}
      </div>
      <div class="text-body-2">{{ t('orderTracking.number') }} : {{ tracking.trackingNumber }}</div>
    </div>
    <v-btn
      v-if="tracking.trackingUrl"
      :href="tracking.trackingUrl"
      target="_blank"
      rel="noopener noreferrer"
      color="primary"
      variant="tonal"
      append-icon="mdi-open-in-new"
    >
      {{ t('orderTracking.follow') }}
    </v-btn>
  </v-card>
</template>
