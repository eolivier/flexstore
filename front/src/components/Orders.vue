<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { formatPrice } from '@/utils/price';

  const { t, locale } = useI18n();

  type OrderStatus = 'shipped' | 'processing' | 'delivered' | 'pendingPayment';

  const statusColors: Record<OrderStatus, string> = {
    shipped: 'info',
    processing: 'warning',
    delivered: 'success',
    pendingPayment: 'error',
  };

  // Demo data: orders are not stored by the back end yet
  const orders: { id: string; customer: string; items: string[]; total: number; status: OrderStatus }[] = [
    { id: 'ORD-001', customer: 'John Doe', items: ['Organic Cotton T-shirt', 'Bluetooth Headphones'], total: 84, status: 'shipped' },
    { id: 'ORD-002', customer: 'Alice Smith', items: ['Waterproof Jacket', 'Smartwatch'], total: 178, status: 'processing' },
    { id: 'ORD-003', customer: 'Michael Brown', items: ['Slim Jeans', '5G Smartphone'], total: 448, status: 'delivered' },
    { id: 'ORD-004', customer: 'Emma Wilson', items: ['Laptop', 'Urban Backpack'], total: 838, status: 'pendingPayment' },
    { id: 'ORD-005', customer: 'David Johnson', items: ['10" Tablet', 'Organic Cotton T-shirt'], total: 274, status: 'shipped' },
  ];
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('orders.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">{{ t('orders.demo') }}</p>

    <v-card>
      <template v-for="(order, index) in orders" :key="order.id">
        <v-divider v-if="index > 0" />
        <div class="order d-flex flex-wrap align-center ga-4 pa-5">
          <v-avatar color="surface-variant" rounded="lg" size="48">
            <v-icon icon="mdi-package-variant-closed" color="primary" />
          </v-avatar>
          <div class="order-info flex-grow-1">
            <div class="text-subtitle-1 font-weight-bold">{{ order.id }} · {{ order.customer }}</div>
            <div class="text-body-2 text-medium-emphasis">{{ order.items.join(', ') }}</div>
          </div>
          <v-chip :color="statusColors[order.status]" variant="tonal" size="small" class="font-weight-medium">
            {{ t(`orders.status.${order.status}`) }}
          </v-chip>
          <div class="order-total text-subtitle-1 font-weight-bold text-right">
            {{ formatPrice(order.total, '€', locale) }}
          </div>
        </div>
      </template>
    </v-card>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
  .order-info {
    min-width: 200px;
  }
  .order-total {
    min-width: 96px;
  }
</style>
