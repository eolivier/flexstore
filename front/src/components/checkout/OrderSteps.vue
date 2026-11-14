<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // The breadcrumb of the order: the pictogram of the current step is selected, the steps already done lead back
  // to their page, so that the customer can change their cart from the checkout.
  // Once the order is confirmed, nothing leads back: the cart and the payment are over
  const props = defineProps<{ current: 'cart' | 'delivery' | 'payment' | 'confirmation' }>();

  const { t } = useI18n();

  const steps = [
    { name: 'cart', icon: 'mdi-cart-outline', to: '/cart' },
    { name: 'delivery', icon: 'mdi-truck-delivery-outline', to: '/checkout' },
    { name: 'payment', icon: 'mdi-credit-card-outline', to: undefined },
    { name: 'confirmation', icon: 'mdi-check-circle-outline', to: undefined },
  ] as const;
  const currentIndex = () => steps.findIndex((step) => step.name === props.current);
  const leadsBack = (index: number) =>
    index < currentIndex() && props.current !== 'confirmation' && steps[index].to !== undefined;
</script>

<template>
  <nav :aria-label="t('cart.steps.label')" class="mb-6">
    <ol class="d-flex align-center justify-center pa-0">
      <template v-for="(step, index) in steps" :key="step.name">
        <v-icon v-if="index > 0" icon="mdi-chevron-right" class="mx-2 text-medium-emphasis" />
        <li :aria-current="step.name === current ? 'step' : undefined" class="step">
          <component
            :is="leadsBack(index) ? 'router-link' : 'span'"
            :to="leadsBack(index) ? step.to : undefined"
            class="d-flex align-center ga-2 text-decoration-none"
            :class="index > currentIndex() ? 'text-disabled' : 'text-primary'"
          >
            <v-avatar
              size="36"
              :color="step.name === current ? 'secondary' : undefined"
              :variant="step.name === current ? 'flat' : 'outlined'"
            >
              <v-icon :icon="step.icon" />
            </v-avatar>
            <span :class="{ 'font-weight-bold': step.name === current }">{{ t(`cart.steps.${step.name}`) }}</span>
          </component>
        </li>
      </template>
    </ol>
  </nav>
</template>

<style scoped>
  ol {
    list-style: none;
  }
</style>
