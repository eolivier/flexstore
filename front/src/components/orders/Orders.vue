<script setup lang="ts">
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useOrdersStore } from '@/stores/orders.store';
  import OrderSummaryCard from './OrderSummaryCard.vue';

  const { t } = useI18n();
  const ordersStore = useOrdersStore();

  onMounted(() => {
    ordersStore.fetchOrders();
  });
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
      <OrderSummaryCard v-for="order in ordersStore.orders" :key="order.id" :order="order" />
    </div>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
</style>
