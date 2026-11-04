<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import {
    CARRIERS,
    isValidTrackingNumber,
    MAX_TRACKING_NUMBER_LENGTH,
    type TrackingInput,
  } from '@/utils/tracking';

  // The carrier and the tracking number of a parcel; the number is told invalid once typed
  const tracking = defineModel<TrackingInput>({ required: true });

  const { t } = useI18n();

  const carrierItems = computed(() =>
    CARRIERS.map((carrier) => ({ value: carrier, title: t(`orderTracking.carriers.${carrier}`) }))
  );
  const numberIsInvalid = computed(
    () => tracking.value.trackingNumber !== '' && !isValidTrackingNumber(tracking.value.trackingNumber)
  );
</script>

<template>
  <v-select
    v-model="tracking.carrier"
    :items="carrierItems"
    :label="t('orderTracking.carrier')"
    data-test="tracking-carrier"
    class="mb-2"
  />
  <v-text-field
    v-model="tracking.trackingNumber"
    :label="t('orderTracking.number')"
    :counter="MAX_TRACKING_NUMBER_LENGTH"
    :error="numberIsInvalid"
    :error-messages="numberIsInvalid ? [t('orderTracking.numberInvalid')] : []"
    data-test="tracking-number"
  />
</template>
