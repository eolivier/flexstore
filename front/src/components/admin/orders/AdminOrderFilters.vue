<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { orderStatusSteps } from '@/utils/order';
  import type { AdminOrderFilter } from '@/utils/adminOrderFilter';

  // The filters of the list of orders: a text (number, customer, recipient) and a status
  const filter = defineModel<AdminOrderFilter>({ required: true });

  const { t } = useI18n();

  const statuses = computed(() =>
    [...orderStatusSteps, 'CANCELLED' as const].map((status) => ({
      title: t(`orders.status.${status}`),
      value: status,
    }))
  );
</script>

<template>
  <v-row dense class="mb-2">
    <v-col cols="12" md="8">
      <v-text-field
        v-model="filter.text"
        :label="t('adminOrders.search')"
        prepend-inner-icon="mdi-magnify"
        clearable
        hide-details
        @click:clear="filter.text = ''"
      />
    </v-col>
    <v-col cols="12" md="4">
      <v-select
        v-model="filter.status"
        :items="statuses"
        :label="t('adminOrders.status')"
        clearable
        hide-details
      />
    </v-col>
  </v-row>
</template>
