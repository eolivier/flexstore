<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { formatAddress } from '@/utils/address';

  // Where the order is delivered and billed
  const props = defineProps<{ order: JsonOrder }>();

  const { t } = useI18n();

  const delivery = computed(() => formatAddress(props.order.deliveryAddress));
  const billing = computed(() => formatAddress(props.order.billingAddress));
</script>

<template>
  <v-card class="pa-5 mt-6">
    <v-row>
      <v-col cols="12" sm="6">
        <div class="text-caption text-medium-emphasis">{{ t('checkout.deliveryAddress') }}</div>
        <div class="text-body-2">{{ delivery }}</div>
      </v-col>
      <v-col cols="12" sm="6">
        <div class="text-caption text-medium-emphasis">{{ t('checkout.billingAddress') }}</div>
        <div class="text-body-2">{{ billing === delivery ? t('checkout.sameAsDelivery') : billing }}</div>
      </v-col>
    </v-row>
  </v-card>
</template>
