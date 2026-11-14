<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { useOrdersStore } from '@/stores/orders.store';
  import { openExternalPage } from '@/utils/navigation';
  import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';

  // Pays an order awaiting its payment, on the checkout page of the payment provider
  const props = defineProps<{ order: JsonOrder }>();
  // The order was paid or cancelled in the meantime: the page reloads it
  const emit = defineEmits<{ (e: 'statusChanged'): void }>();

  const { t } = useI18n();
  const ordersStore = useOrdersStore();

  const starting = ref(false);
  const paymentError = ref(false);

  async function pay() {
    starting.value = true;
    paymentError.value = false;
    const paymentStart = await ordersStore.startPayment(props.order.id);
    starting.value = false;
    if (paymentStart.status === 'started') {
      openExternalPage(paymentStart.checkoutUrl);
    } else if (paymentStart.status === 'statusChanged') {
      emit('statusChanged');
    } else {
      paymentError.value = true;
    }
  }
</script>

<template>
  <div>
    <FeedbackAlert v-if="paymentError" type="error" compact class="mb-3">
      {{ t('orders.paymentError') }}
    </FeedbackAlert>
    <v-btn color="secondary" size="large" prepend-icon="mdi-credit-card-outline" :loading="starting" @click="pay">
      {{ t('orders.pay') }}
    </v-btn>
  </div>
</template>
