<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { isCompleteTracking, NO_TRACKING, type TrackingInput } from '@/utils/tracking';
  import TrackingFields from './TrackingFields.vue';

  // Asks for the carrier and the tracking number when an order is shipped; an order can also be shipped without
  const open = defineModel<boolean>({ required: true });
  const emit = defineEmits<{ confirm: [tracking: TrackingInput] }>();

  const { t } = useI18n();

  const tracking = ref<TrackingInput>({ ...NO_TRACKING });

  watch(open, (isOpen) => {
    if (isOpen) tracking.value = { ...NO_TRACKING };
  });

  function confirm(given: TrackingInput) {
    open.value = false;
    emit('confirm', given);
  }
</script>

<template>
  <v-dialog v-model="open" max-width="480">
    <v-card class="pa-5">
      <h2 class="text-h6 font-weight-bold mb-1">{{ t('orderTracking.shipTitle') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-4">{{ t('orderTracking.shipText') }}</p>
      <TrackingFields v-model="tracking" />
      <div class="d-flex flex-wrap justify-end ga-2">
        <v-btn variant="text" data-test="ship-cancel" @click="open = false">{{ t('cancel') }}</v-btn>
        <v-btn variant="outlined" data-test="ship-without" @click="confirm({ ...NO_TRACKING })">
          {{ t('orderTracking.shipWithout') }}
        </v-btn>
        <v-btn
          color="primary"
          :disabled="!isCompleteTracking(tracking)"
          data-test="ship-with"
          @click="confirm({ ...tracking })"
        >
          {{ t('orderTracking.ship') }}
        </v-btn>
      </div>
    </v-card>
  </v-dialog>
</template>
