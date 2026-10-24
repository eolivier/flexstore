<script setup lang="ts">
  import { computed, onMounted, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useOrdersStore, type OrderLookup } from '@/stores/orders.store';
  import { orderItemsCount, orderNumber, placedOn } from '@/utils/order';
  import OrderStatusSteps from './OrderStatusSteps.vue';
  import OrderLinesTable from './OrderLinesTable.vue';

  const props = defineProps<{ id: string }>();

  const { t, locale } = useI18n();
  const ordersStore = useOrdersStore();

  const orderLookup = ref<OrderLookup | null>(null);
  const order = computed(() => (orderLookup.value?.status === 'found' ? orderLookup.value.order : null));

  async function load() {
    orderLookup.value = null;
    orderLookup.value = await ordersStore.fetchOrder(props.id);
  }

  onMounted(load);
  watch(() => props.id, load);
</script>

<template>
  <v-container class="page py-10">
    <v-btn to="/orders" variant="text" prepend-icon="mdi-arrow-left" class="mb-4 px-2">
      {{ t('orders.backToOrders') }}
    </v-btn>

    <v-skeleton-loader v-if="!orderLookup" type="heading, list-item-two-line@3" class="rounded-xl" />

    <v-card v-else-if="orderLookup.status === 'notFound'" class="pa-10 text-center">
      <v-icon icon="mdi-package-variant-remove" size="56" class="text-medium-emphasis mb-3" />
      <h1 class="text-h6 font-weight-bold mb-1">{{ t('orders.notFound') }}</h1>
      <p class="text-body-2 text-medium-emphasis mb-6">{{ t('orders.notFoundText') }}</p>
      <v-btn to="/orders" color="secondary">{{ t('orders.backToOrders') }}</v-btn>
    </v-card>

    <v-alert v-else-if="orderLookup.status === 'error'" type="error" variant="tonal">
      {{ t('orders.orderLoadError') }}
    </v-alert>

    <template v-else-if="order">
      <v-card class="pa-5 mb-6">
        <OrderStatusSteps :status="order.status" />
      </v-card>
      <h1 class="text-h4 font-weight-bold">{{ t('orders.number', { number: orderNumber(order) }) }}</h1>
      <p class="text-body-2 text-medium-emphasis mt-1 mb-6">
        {{ t('orders.placedOn', { date: placedOn(order, locale) }) }} · {{ t('orders.itemsCount', orderItemsCount(order)) }}
      </p>
      <OrderLinesTable :order="order" />
    </template>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
</style>
