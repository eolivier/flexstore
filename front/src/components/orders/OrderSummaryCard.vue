<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { formatPrice } from '@/utils/price';
  import { orderItemsCount, orderNumber, orderStatusColors, placedOn } from '@/utils/order';

  const props = defineProps<{ order: JsonOrder }>();

  const { t, locale } = useI18n();

  const linesSummary = props.order.lines.map(line => `${line.quantity} × ${line.productName}`).join(', ');
</script>

<template>
  <v-card :to="`/orders/${order.id}`" :aria-label="t('orders.viewDetail', { number: orderNumber(order) })" class="pa-5">
    <div class="d-flex flex-wrap align-center ga-4">
      <v-avatar color="surface-variant" rounded="lg" size="48">
        <v-icon icon="mdi-package-variant-closed" color="primary" />
      </v-avatar>
      <div class="order-info flex-grow-1">
        <div class="text-subtitle-1 font-weight-bold">{{ t('orders.number', { number: orderNumber(order) }) }}</div>
        <div class="text-body-2 text-medium-emphasis">
          {{ t('orders.placedOn', { date: placedOn(order, locale) }) }} · {{ t('orders.itemsCount', orderItemsCount(order)) }}
        </div>
        <div class="lines-summary text-body-2 text-medium-emphasis">{{ linesSummary }}</div>
      </div>
      <v-chip :color="orderStatusColors[order.status]" variant="tonal" size="small" class="font-weight-medium">
        {{ t(`orders.status.${order.status}`) }}
      </v-chip>
      <div class="order-total text-subtitle-1 font-weight-bold text-right">
        {{ formatPrice(order.totalPrice, order.currency, locale) }}
      </div>
      <v-icon icon="mdi-chevron-right" class="text-medium-emphasis" />
    </div>
  </v-card>
</template>

<style scoped>
  .order-info {
    min-width: 200px;
  }
  .lines-summary {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 420px;
  }
  .order-total {
    min-width: 96px;
  }
</style>
