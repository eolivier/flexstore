<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrderTracking } from '@/generated';
  import { isCompleteTracking, NO_TRACKING, type TrackingInput } from '@/utils/tracking';
  import FormDialog from '@/components/layout/FormDialog.vue';
  import TrackingFields from './TrackingFields.vue';

  // Corrects the carrier and the tracking number of an order shipped or delivered
  const props = defineProps<{ current: JsonOrderTracking | null; saving: boolean }>();
  const emit = defineEmits<{ correct: [tracking: TrackingInput] }>();

  const { t } = useI18n();

  const open = ref(false);
  const tracking = ref<TrackingInput>({ ...NO_TRACKING });

  function show() {
    tracking.value = props.current
      ? { carrier: props.current.carrier, trackingNumber: props.current.trackingNumber }
      : { ...NO_TRACKING };
    open.value = true;
  }

  function save() {
    open.value = false;
    emit('correct', { ...tracking.value });
  }
</script>

<template>
  <v-btn
    variant="outlined"
    size="small"
    prepend-icon="mdi-pencil-outline"
    :loading="saving"
    data-test="edit-tracking"
    @click="show"
  >
    {{ t('orderTracking.edit') }}
  </v-btn>
  <FormDialog v-model="open" :title="t('orderTracking.editTitle')">
    <TrackingFields v-model="tracking" />
    <template #actions>
      <v-btn color="secondary" :disabled="!isCompleteTracking(tracking)" data-test="save-tracking" @click="save">
        {{ t('save') }}
      </v-btn>
    </template>
  </FormDialog>
</template>
