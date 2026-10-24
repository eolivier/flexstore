<script setup lang="ts">
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useOrdersStore } from '@/stores/orders.store';
  import { formatPrice } from '@/utils/price';
  import { orderNumber, orderStatusColors, placedOn } from '@/utils/order';
  import type { JsonOrder } from '@/generated';

  const { t, locale } = useI18n();
  const ordersStore = useOrdersStore();

  onMounted(() => {
    ordersStore.fetchOrders();
  });

  function itemsCount(order: JsonOrder) {
    return order.lines.reduce((count, line) => count + line.quantity, 0);
  }

  function linesSummary(order: JsonOrder) {
    return order.lines.map(line => `${line.quantity} × ${line.productName}`).join(', ');
  }
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('orders.title') }}</h1>

    <v-alert v-if="ordersStore.loadError" type="error" variant="tonal" class="mb-6">
      {{ t('orders.loadError') }}
    </v-alert>

    <v-skeleton-loader v-else-if="!ordersStore.orders" type="list-item-avatar-two-line@3" class="rounded-xl" />

    <v-card v-else-if="ordersStore.orders.length === 0" class="pa-10 text-center">
      <v-icon icon="mdi-package-variant" size="56" class="text-medium-emphasis mb-3" />
      <h2 class="text-h6 font-weight-bold mb-1">{{ t('orders.empty') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-6">{{ t('orders.emptyText') }}</p>
      <v-btn to="/products/" color="secondary" size="large" append-icon="mdi-arrow-right">
        {{ t('home.heroButton') }}
      </v-btn>
    </v-card>

    <div v-else class="d-flex flex-column ga-4">
      <v-card
        v-for="order in ordersStore.orders"
        :key="order.id"
        :to="`/orders/${order.id}`"
        :aria-label="t('orders.viewDetail', { number: orderNumber(order) })"
        class="pa-5"
      >
        <div class="d-flex flex-wrap align-center ga-4">
          <v-avatar color="surface-variant" rounded="lg" size="48">
            <v-icon icon="mdi-package-variant-closed" color="primary" />
          </v-avatar>
          <div class="order-info flex-grow-1">
            <div class="text-subtitle-1 font-weight-bold">{{ t('orders.number', { number: orderNumber(order) }) }}</div>
            <div class="text-body-2 text-medium-emphasis">
              {{ t('orders.placedOn', { date: placedOn(order, locale) }) }} · {{ t('orders.itemsCount', itemsCount(order)) }}
            </div>
            <div class="lines-summary text-body-2 text-medium-emphasis">{{ linesSummary(order) }}</div>
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
    </div>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
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
