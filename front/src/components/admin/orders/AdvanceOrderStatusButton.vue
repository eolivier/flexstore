<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { hasLeftItsLifeCycle, isMovedByAnAdministrator, nextStatus } from '@/utils/order';
  import { isShippedNext, NO_TRACKING, type TrackingInput } from '@/utils/tracking';
  import ShipOrderDialog from './ShipOrderDialog.vue';

  // Moves an order to the next status of its life cycle; the payment moves an order awaiting it,
  // and a delivered order has no next status. A processed order is shipped from a dialog that asks for the
  // carrier and the tracking number of its parcel
  const props = defineProps<{ order: JsonOrder; advancing: boolean }>();
  const emit = defineEmits<{ advance: [order: JsonOrder, tracking: TrackingInput] }>();

  const { t } = useI18n();

  const next = computed(() => t(`orders.status.${nextStatus(props.order.status)}`));
  const askingTracking = ref(false);

  function advance() {
    if (isShippedNext(props.order.status)) {
      askingTracking.value = true;
    } else {
      emit('advance', props.order, { ...NO_TRACKING });
    }
  }
</script>

<template>
  <v-btn
    v-if="isMovedByAnAdministrator(order.status)"
    :loading="advancing"
    color="primary"
    variant="tonal"
    size="small"
    append-icon="mdi-arrow-right"
    @click="advance"
  >
    {{ t('adminOrders.advanceTo', { status: next }) }}
  </v-btn>
  <span v-else-if="hasLeftItsLifeCycle(order.status)" class="text-caption text-error">
    {{ t(`orders.status.${order.status}`) }}
  </span>
  <span v-else-if="order.status === 'AWAITING_PAYMENT'" class="text-caption text-medium-emphasis">
    {{ t('adminOrders.awaitingPayment') }}
  </span>
  <span v-else class="text-caption text-medium-emphasis">{{ t('adminOrders.lastStatus') }}</span>
  <ShipOrderDialog v-model="askingTracking" @confirm="(tracking) => emit('advance', order, tracking)" />
</template>
