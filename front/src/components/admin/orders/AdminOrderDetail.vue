<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { computed, onMounted, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { useAdminOrdersStore, type AdminOrderLookup } from '@/stores/adminOrders.store';
  import { isInvoiced, orderItemsCount, orderNumber, placedOn } from '@/utils/order';
  import OrderStatusSteps from '@/components/orders/OrderStatusSteps.vue';
  import OrderLinesTable from '@/components/orders/OrderLinesTable.vue';
  import OrderAddressesCard from '@/components/orders/OrderAddressesCard.vue';
  import AdminOrderCustomerCard from './AdminOrderCustomerCard.vue';
  import AdminOrderStatusHistory from './AdminOrderStatusHistory.vue';
  import AdvanceOrderStatusButton from './AdvanceOrderStatusButton.vue';
  import RefundOrderButton from './RefundOrderButton.vue';
  import InvoiceDownloadButton from '@/components/orders/InvoiceDownloadButton.vue';

  // An order of any customer, with the customer who placed it: the administrator moves it to its next status
  const props = defineProps<{ id: string }>();

  const { t, locale } = useI18n();
  const adminOrdersStore = useAdminOrdersStore();

  const orderLookup = ref<AdminOrderLookup | null>(null);
  const order = computed(() => (orderLookup.value?.status === 'found' ? orderLookup.value.order : null));
  const advancing = ref(false);
  const refunding = ref(false);
  const feedback = ref<string | null>(null);

  async function load() {
    orderLookup.value = await adminOrdersStore.fetchOrder(props.id);
  }

  async function advance(shownOrder: JsonOrder) {
    advancing.value = true;
    feedback.value = null;
    const statusAdvance = await adminOrdersStore.advanceStatus(shownOrder);
    if (statusAdvance !== 'advanced') feedback.value = t(`adminOrders.${statusAdvance}`);
    await load();
    advancing.value = false;
  }

  async function refund(shownOrder: JsonOrder) {
    refunding.value = true;
    feedback.value = null;
    const refundOutcome = await adminOrdersStore.refundOrder(shownOrder);
    if (refundOutcome !== 'refunded') feedback.value = t(`adminOrders.${refundOutcome}`);
    await load();
    refunding.value = false;
  }

  onMounted(load);
  watch(() => props.id, load);
</script>

<template>
  <v-container class="page py-10">
    <AdminBreadcrumbs />
    <v-skeleton-loader v-if="!orderLookup" type="heading, list-item-two-line@3" class="rounded-xl" />
    <v-alert v-else-if="orderLookup.status === 'notFound'" type="warning" variant="tonal">
      {{ t('orders.notFound') }}
    </v-alert>
    <v-alert v-else-if="orderLookup.status === 'error'" type="error" variant="tonal">
      {{ t('orders.orderLoadError') }}
    </v-alert>
    <template v-else-if="order">
      <div class="d-flex flex-wrap align-end ga-4 mb-6">
        <div>
          <h1 class="text-h4 font-weight-bold">{{ t('orders.number', { number: orderNumber(order) }) }}</h1>
          <p class="text-body-2 text-medium-emphasis mt-1">
            {{ t('orders.placedOn', { date: placedOn(order, locale) }) }} ·
            {{ t('orders.itemsCount', orderItemsCount(order)) }}
          </p>
        </div>
        <v-spacer />
        <div class="d-flex flex-wrap align-start ga-2">
          <InvoiceDownloadButton
            v-if="isInvoiced(order.status)"
            :order="order"
            :invoice-path="`/api/admin/orders/${encodeURIComponent(order.id)}/invoice`"
          />
          <RefundOrderButton :order="order" :refunding="refunding" @refund="refund" />
          <AdvanceOrderStatusButton :order="order" :advancing="advancing" @advance="advance" />
        </div>
      </div>
      <v-alert v-if="feedback" type="warning" variant="tonal" class="mb-4">{{ feedback }}</v-alert>
      <v-alert v-if="order.status === 'CANCELLED'" type="warning" variant="tonal" class="mb-6">
        {{ t('adminOrders.cancelled') }}
      </v-alert>
      <v-alert v-else-if="order.status === 'REFUNDED'" type="info" variant="tonal" class="mb-6">
        {{ t('adminOrders.refunded') }}
      </v-alert>
      <v-card v-else class="pa-5 mb-6">
        <OrderStatusSteps :status="order.status" />
      </v-card>
      <AdminOrderCustomerCard :customer="order.customer" />
      <AdminOrderStatusHistory :status-history="order.statusHistory" />
      <OrderLinesTable :order="order" />
      <OrderAddressesCard :order="order" />
    </template>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
</style>
