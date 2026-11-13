<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { isCompleteTracking, NO_TRACKING, type TrackingInput } from '@/utils/tracking';
  import FormDialog from '@/components/layout/FormDialog.vue';
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
  <FormDialog v-model="open" :title="t('orderTracking.shipTitle')" :text="t('orderTracking.shipText')">
    <TrackingFields v-model="tracking" />
    <template #actions>
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
    </template>
  </FormDialog>
</template>
