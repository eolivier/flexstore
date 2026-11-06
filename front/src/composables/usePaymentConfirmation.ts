import { computed, onUnmounted, ref } from 'vue';
import { useOrdersStore, type OrderLookup } from '@/stores/orders.store';

export const CONFIRMATION_CHECKS = 10;
const CONFIRMATION_CHECK_DELAY_MS = 2000;

// The order of a customer who just paid: the payment provider may confirm the payment a little later, so the
// order is read again every 2 seconds (10 times at most) while it still awaits its payment. Its notification
// may also be lost: the provider is asked once, when the customer comes back
export function usePaymentConfirmation(orderId: () => string, onSettled: () => Promise<void> | void = () => {}) {
  const ordersStore = useOrdersStore();
  const orderLookup = ref<OrderLookup | null>(null);
  const order = computed(() => (orderLookup.value?.status === 'found' ? orderLookup.value.order : null));
  const awaitingPayment = computed(() => order.value?.status === 'AWAITING_PAYMENT');
  // True while the payment is awaited and the order is still going to be read again
  const confirming = ref(false);
  let nextCheck: ReturnType<typeof setTimeout> | undefined;

  async function load(remainingChecks = CONFIRMATION_CHECKS) {
    clearTimeout(nextCheck);
    if (remainingChecks === CONFIRMATION_CHECKS) await ordersStore.verifyPayment(orderId());
    orderLookup.value = await ordersStore.fetchOrder(orderId());
    confirming.value = awaitingPayment.value && remainingChecks > 0;
    if (confirming.value) {
      nextCheck = setTimeout(() => load(remainingChecks - 1), CONFIRMATION_CHECK_DELAY_MS);
    } else {
      await onSettled();
    }
  }

  onUnmounted(() => clearTimeout(nextCheck));

  return { orderLookup, order, awaitingPayment, confirming, load };
}
