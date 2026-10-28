<script setup lang="ts">
  import { computed, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { useNow } from '@/composables/useNow';
  import { timeLeftToPay } from '@/utils/order';

  // The time left to pay the order, counted down every second; once it is over, the page is told so that it
  // reads the order again (cancelled by the shop)
  const props = defineProps<{ order: JsonOrder }>();
  const emit = defineEmits<{ expired: [] }>();

  const { t } = useI18n();
  const { now } = useNow();

  const timeLeft = computed(() => timeLeftToPay(props.order, now.value));

  watch(timeLeft, (current, previous) => {
    if (current === null && previous !== null) emit('expired');
  });
</script>

<template>
  <div v-if="timeLeft" class="mb-3">
    <p class="d-flex align-center ga-2 text-body-1 font-weight-medium" role="timer" aria-live="off">
      <v-icon icon="mdi-timer-sand" color="warning" />
      <span>{{ t('orders.timeLeftToPay') }}</span>
      <span class="countdown">{{ timeLeft }}</span>
    </p>
    <p class="text-body-2 text-medium-emphasis mt-1">{{ t('orders.afterPaymentDelay') }}</p>
  </div>
  <p v-else class="text-body-2 text-medium-emphasis mb-3">{{ t('orders.paymentDelayOver') }}</p>
</template>

<style scoped>
  .countdown {
    font-variant-numeric: tabular-nums;
    font-weight: 700;
  }
</style>
