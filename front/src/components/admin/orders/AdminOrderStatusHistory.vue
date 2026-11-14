<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonOrderStatusChange } from '@/generated';
  import { formatDateTime } from '@/utils/date';

  // The statuses an order went through, oldest first, with the day and the time it reached each one
  defineProps<{ statusHistory: JsonOrderStatusChange[] }>();

  const { t, locale } = useI18n();

  function changedOn(statusChange: JsonOrderStatusChange) {
    return formatDateTime(statusChange.changedAt, locale.value);
  }
</script>

<template>
  <v-card class="pa-5 mb-6">
    <h2 class="text-subtitle-1 font-weight-bold mb-2">{{ t('adminOrders.history') }}</h2>
    <v-timeline density="compact" side="end" truncate-line="both">
      <v-timeline-item
        v-for="(statusChange, index) in statusHistory"
        :key="index"
        :dot-color="index === statusHistory.length - 1 ? 'primary' : 'grey'"
        size="x-small"
      >
        <div class="d-flex flex-wrap ga-2 align-baseline">
          <span class="font-weight-medium">{{ t(`orders.status.${statusChange.status}`) }}</span>
          <span class="text-caption text-medium-emphasis">{{ changedOn(statusChange) }}</span>
        </div>
      </v-timeline-item>
    </v-timeline>
  </v-card>
</template>
