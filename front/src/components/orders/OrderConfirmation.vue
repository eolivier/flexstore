<script setup lang="ts">
  import { computed, onMounted, onUnmounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useOrdersStore, type OrderLookup } from '@/stores/orders.store';
  import { formatPrice } from '@/utils/price';
  import { orderItemsCount, orderNumber, placedOn } from '@/utils/order';

  // The page the customer comes back to after paying: the payment provider may confirm the payment a little later
  const props = defineProps<{ id: string }>();
  const CONFIRMATION_CHECKS = 10;
  const CONFIRMATION_CHECK_DELAY_MS = 2000;

  const { t, locale } = useI18n();
  const ordersStore = useOrdersStore();

  const orderLookup = ref<OrderLookup | null>(null);
  const order = computed(() =>
    orderLookup.value?.status === 'found' ? orderLookup.value.order : null
  );
  const orderPath = computed(() => `/orders/${props.id}`);

  const awaitingPayment = computed(() => order.value?.status === 'AWAITING_PAYMENT');
  let confirmationCheck: ReturnType<typeof setTimeout> | undefined;

  async function load(remainingChecks: number) {
    orderLookup.value = await ordersStore.fetchOrder(props.id);
    if (awaitingPayment.value && remainingChecks > 0) {
      confirmationCheck = setTimeout(() => load(remainingChecks - 1), CONFIRMATION_CHECK_DELAY_MS);
    } else {
      // Once paid, the cart of the customer is empty
      await ordersStore.refreshCart();
    }
  }

  onMounted(() => load(CONFIRMATION_CHECKS));
  onUnmounted(() => clearTimeout(confirmationCheck));
</script>

<template>
  <v-container class="page py-12">
    <v-skeleton-loader v-if="!orderLookup" type="article" class="rounded-xl" />

    <v-card v-else-if="order" class="pa-8 pa-sm-10 text-center">
      <v-avatar color="success" variant="tonal" size="72" class="mb-4">
        <v-icon icon="mdi-check-bold" size="40" />
      </v-avatar>
      <h1 class="text-h4 font-weight-bold mb-2">{{ t('orders.thanks') }}</h1>
      <i18n-t keypath="orders.confirmation" tag="p" class="text-body-1 text-medium-emphasis mb-6">
        <template #number>
          <router-link :to="orderPath" class="order-link font-weight-bold">{{
            orderNumber(order)
          }}</router-link>
        </template>
        <template #date>{{ placedOn(order, locale) }}</template>
      </i18n-t>

      <v-alert v-if="awaitingPayment" type="info" variant="tonal" class="mb-6 text-start">
        {{ t('orders.awaitingPaymentConfirmation') }}
      </v-alert>

      <v-sheet
        rounded="lg"
        color="surface-variant"
        class="summary d-flex justify-space-between align-center pa-4 mb-8 mx-auto"
      >
        <span class="text-body-2">{{ t('orders.itemsCount', orderItemsCount(order)) }}</span>
        <span class="text-h6 font-weight-bold">{{
          formatPrice(order.totalPrice, order.currency, locale)
        }}</span>
      </v-sheet>

      <div class="d-flex flex-column flex-sm-row justify-center ga-3">
        <v-btn
          :to="orderPath"
          color="secondary"
          size="large"
          prepend-icon="mdi-receipt-text-outline"
        >
          {{ t('orders.seeDetail') }}
        </v-btn>
        <v-btn to="/products/" variant="outlined" color="primary" size="large">
          {{ t('cart.continue') }}
        </v-btn>
      </div>
    </v-card>

    <v-alert v-else type="error" variant="tonal">{{ t('orders.orderLoadError') }}</v-alert>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 720px;
  }
  .summary {
    max-width: 360px;
  }
  .order-link {
    color: rgb(var(--v-theme-secondary));
  }
</style>
