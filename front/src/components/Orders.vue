<script setup lang="ts">
  import { computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import { useOrdersStore } from '@/stores/orders.store';
  import { formatPrice } from '@/utils/price';
  import type { JsonOrder } from '@/generated';

  const { t, locale } = useI18n();
  const route = useRoute();
  const ordersStore = useOrdersStore();

  // Set after an order is placed from the cart
  const placedOrderId = computed(() => route.query.placed as string | undefined);

  const statusColors: Record<JsonOrder['status'], string> = {
    PROCESSING: 'warning',
    SHIPPED: 'info',
    DELIVERED: 'success',
  };

  onMounted(() => {
    ordersStore.fetchOrders();
  });

  function orderNumber(order: JsonOrder) {
    return order.id.slice(0, 8).toUpperCase();
  }

  function placedOn(order: JsonOrder) {
    return new Date(order.placedAt).toLocaleDateString(locale.value, { day: 'numeric', month: 'long', year: 'numeric' });
  }
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('orders.title') }}</h1>

    <v-alert v-if="placedOrderId" type="success" variant="tonal" class="mb-6" icon="mdi-check-circle-outline">
      {{ t('orders.placed') }}
    </v-alert>

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
        :class="['pa-5', { 'order--placed': order.id === placedOrderId }]"
      >
        <div class="d-flex flex-wrap align-center ga-4">
          <v-avatar color="surface-variant" rounded="lg" size="48">
            <v-icon icon="mdi-package-variant-closed" color="primary" />
          </v-avatar>
          <div class="flex-grow-1">
            <div class="text-subtitle-1 font-weight-bold">{{ t('orders.number', { number: orderNumber(order) }) }}</div>
            <div class="text-body-2 text-medium-emphasis">{{ t('orders.placedOn', { date: placedOn(order) }) }}</div>
          </div>
          <v-chip :color="statusColors[order.status]" variant="tonal" size="small" class="font-weight-medium">
            {{ t(`orders.status.${order.status}`) }}
          </v-chip>
          <div class="order-total text-subtitle-1 font-weight-bold text-right">
            {{ formatPrice(order.totalPrice, order.currency, locale) }}
          </div>
        </div>
        <v-divider class="my-4" />
        <div v-for="line in order.lines" :key="line.productId" class="d-flex text-body-2 py-1">
          <router-link :to="`/products/${line.productId}`" class="line-name text-high-emphasis">
            {{ line.quantity }} × {{ line.productName }}
          </router-link>
          <v-spacer />
          <span class="text-medium-emphasis">{{ formatPrice(line.linePrice, order.currency, locale) }}</span>
        </div>
      </v-card>
    </div>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
  .order-total {
    min-width: 96px;
  }
  .order--placed {
    border-color: rgb(var(--v-theme-success)) !important;
  }
  .line-name {
    text-decoration: none;
  }
  .line-name:hover {
    text-decoration: underline;
  }
</style>
