<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { orderStatusSteps, orderStepState, type OrderStepsLayout } from '@/utils/order';
  import OrderStatusStep from './OrderStatusStep.vue';

  const props = withDefaults(defineProps<{ status: JsonOrder['status']; layout?: OrderStepsLayout }>(), { layout: 'full' });

  const { t } = useI18n();

  const steps = computed(() => orderStatusSteps.map(step => ({ step, state: orderStepState(step, props.status) })));
</script>

<template>
  <nav :aria-label="t('orders.progress')">
    <ol :class="['steps', `steps--${layout}`, 'd-flex', 'align-center']">
      <template v-for="({ step, state }, index) in steps" :key="step">
        <li v-if="index > 0" class="connector" :class="{ 'connector--reached': state !== 'upcoming' }" aria-hidden="true" />
        <OrderStatusStep :step="step" :state="state" :layout="layout" />
      </template>
    </ol>
  </nav>
</template>

<style scoped>
  .steps {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  .connector {
    flex: 1;
    min-width: 16px;
    height: 2px;
    margin: 0 12px;
    background: rgba(var(--v-border-color), 0.2);
  }
  .connector--reached {
    background: rgb(var(--v-theme-primary));
  }
  .steps--compact .connector {
    flex: 0 0 14px;
    min-width: 14px;
    margin: 0 4px;
  }
  @media (max-width: 599px) {
    .steps--full .connector {
      margin: 0 4px 22px;
    }
  }
</style>
