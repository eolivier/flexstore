<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import type { OrderStepsLayout, OrderStepState } from '@/utils/order';

  defineProps<{ step: JsonOrder['status']; state: OrderStepState; layout: OrderStepsLayout }>();

  const { t } = useI18n();

  const stepIcons: Record<JsonOrder['status'], string> = {
    AWAITING_PAYMENT: 'mdi-credit-card-clock-outline',
    PAID: 'mdi-credit-card-check-outline',
    PROCESSING: 'mdi-package-variant-closed',
    SHIPPED: 'mdi-truck-outline',
    DELIVERED: 'mdi-home-outline',
  };
</script>

<template>
  <li
    :class="['step', `step--${state}`, `step--${layout}`]"
    :aria-current="state === 'current' ? 'step' : undefined"
  >
    <span class="step-marker d-flex align-center justify-center">
      <v-icon :icon="state === 'done' ? 'mdi-check' : stepIcons[step]" :size="layout === 'compact' ? 14 : 18" />
    </span>
    <!-- In the compact layout, only the current step shows its label;
         the others stay readable by screen readers -->
    <span :class="['step-label', { 'd-sr-only': layout === 'compact' && state !== 'current' }]">{{
      t(`orders.status.${step}`)
    }}</span>
  </li>
</template>

<style scoped>
  .step {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }
  .step-marker {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border: 2px solid rgba(var(--v-border-color), 0.2);
    color: rgba(var(--v-theme-on-surface), 0.4);
    background: rgb(var(--v-theme-surface));
  }
  .step-label {
    font-size: 0.875rem;
    color: rgba(var(--v-theme-on-surface), 0.5);
  }
  .step--done .step-marker {
    border-color: rgb(var(--v-theme-primary));
    background: rgb(var(--v-theme-primary));
    color: rgb(var(--v-theme-on-primary));
  }
  .step--done .step-label {
    color: rgba(var(--v-theme-on-surface), 0.8);
  }
  /* The current status stands out: filled accent colour, halo, bold label */
  .step--current .step-marker {
    border-color: rgb(var(--v-theme-secondary));
    background: rgb(var(--v-theme-secondary));
    color: rgb(var(--v-theme-on-secondary));
    box-shadow: 0 0 0 4px rgba(var(--v-theme-secondary), 0.2);
  }
  .step--current .step-label {
    color: rgb(var(--v-theme-on-surface));
    font-weight: 700;
  }
  .step--compact {
    gap: 6px;
  }
  .step--compact .step-marker {
    width: 26px;
    height: 26px;
  }
  .step--compact.step--current .step-marker {
    box-shadow: 0 0 0 3px rgba(var(--v-theme-secondary), 0.2);
  }
  @media (max-width: 599px) {
    .step--full {
      flex-direction: column;
      gap: 4px;
      text-align: center;
    }
    .step--full .step-label {
      font-size: 0.75rem;
    }
  }
</style>
