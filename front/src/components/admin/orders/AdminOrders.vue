<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { useAdminOrdersStore } from '@/stores/adminOrders.store';
  import { orderNumber, placedOn } from '@/utils/order';
  import { recipientName } from '@/utils/address';
  import { formatPrice } from '@/utils/price';
  import AdvanceOrderStatusButton from './AdvanceOrderStatusButton.vue';

  // The orders of all the customers: the administrator moves each one to its next status
  const adminOrdersStore = useAdminOrdersStore();
  const { t, locale } = useI18n();

  const advancingOrderId = ref<string | null>(null);
  const feedback = ref<string | null>(null);

  onMounted(() => {
    adminOrdersStore.fetchAllOrders();
  });

  const headers = computed(() => [
    { title: t('adminOrders.number'), key: 'number', sortable: false },
    { title: t('adminOrders.placedOn'), key: 'placedAt' },
    { title: t('adminOrders.recipient'), key: 'recipient', sortable: false },
    { title: t('adminOrders.total'), key: 'totalPrice', align: 'end' as const },
    { title: t('adminOrders.status'), key: 'status' },
    { title: '', key: 'actions', sortable: false, align: 'end' as const },
  ]);

  async function advance(order: JsonOrder) {
    advancingOrderId.value = order.id;
    feedback.value = null;
    const statusAdvance = await adminOrdersStore.advanceStatus(order);
    advancingOrderId.value = null;
    if (statusAdvance !== 'advanced') feedback.value = t(`adminOrders.${statusAdvance}`);
  }
</script>

<template>
  <v-container class="py-10">
    <h1 class="text-h4 font-weight-bold">{{ t('adminOrders.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mt-1 mb-6">
      {{ t('adminOrders.count', adminOrdersStore.orders.length) }}
    </p>

    <v-alert v-if="adminOrdersStore.loadError" type="error" variant="tonal" class="mb-4">
      {{ t('adminOrders.loadError') }}
    </v-alert>
    <v-alert v-if="feedback" type="warning" variant="tonal" class="mb-4">{{ feedback }}</v-alert>

    <v-data-table
      :headers="headers"
      :items="adminOrdersStore.orders"
      :loading="adminOrdersStore.loading"
      :loading-text="t('adminOrders.loading')"
      item-value="id"
    >
      <template #item.number="{ item }">
        <span class="font-weight-medium">{{ orderNumber(item) }}</span>
      </template>
      <template #item.placedAt="{ item }">{{ placedOn(item, locale) }}</template>
      <template #item.recipient="{ item }">{{ recipientName(item.deliveryAddress) }}</template>
      <template #item.totalPrice="{ item }">{{ formatPrice(item.totalPrice, item.currency, locale) }}</template>
      <template #item.status="{ item }">
        <v-chip variant="tonal" size="small">{{ t(`orders.status.${item.status}`) }}</v-chip>
      </template>
      <template #item.actions="{ item }">
        <AdvanceOrderStatusButton :order="item" :advancing="advancingOrderId === item.id" @advance="advance" />
      </template>
    </v-data-table>
  </v-container>
</template>
