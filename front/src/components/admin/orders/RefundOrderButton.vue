<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import ConfirmDialog from '@/components/ConfirmDialog.vue';
  import { isRefundable } from '@/utils/order';

  // Cancels a paid order not shipped yet and refunds it, once the administrator confirms it
  defineProps<{ order: JsonOrder; refunding: boolean }>();
  const emit = defineEmits<{ refund: [order: JsonOrder] }>();

  const { t } = useI18n();

  const confirming = ref(false);
</script>

<template>
  <template v-if="isRefundable(order.status)">
    <v-btn
      :loading="refunding"
      color="error"
      variant="outlined"
      size="small"
      prepend-icon="mdi-cash-refund"
      @click="confirming = true"
    >
      {{ t('adminOrders.refund') }}
    </v-btn>
    <ConfirmDialog
      v-model="confirming"
      :title="t('adminOrders.refundTitle')"
      :message="t('adminOrders.refundMessage')"
      @confirm="emit('refund', order)"
    />
  </template>
</template>
