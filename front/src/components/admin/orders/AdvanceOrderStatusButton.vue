<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { hasNextStatus, nextStatus } from '@/utils/order';

  // Moves an order to the next status of its life cycle; a delivered order has none
  const props = defineProps<{ order: JsonOrder; advancing: boolean }>();
  const emit = defineEmits<{ advance: [order: JsonOrder] }>();

  const { t } = useI18n();

  const next = computed(() => t(`orders.status.${nextStatus(props.order.status)}`));
</script>

<template>
  <v-btn
    v-if="hasNextStatus(order.status)"
    :loading="advancing"
    color="primary"
    variant="tonal"
    size="small"
    append-icon="mdi-arrow-right"
    @click="emit('advance', order)"
  >
    {{ t('adminOrders.advanceTo', { status: next }) }}
  </v-btn>
  <span v-else class="text-caption text-medium-emphasis">{{ t('adminOrders.lastStatus') }}</span>
</template>
