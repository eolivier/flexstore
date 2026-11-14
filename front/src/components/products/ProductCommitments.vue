<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // The commitments of the shop: delivery, returns and payment
  const { t } = useI18n();
</script>

<template>
  <v-divider class="mb-6" />
  <div class="d-flex flex-column ga-3">
    <div class="d-flex align-center text-body-2">
      <v-icon icon="mdi-truck-fast-outline" color="primary" class="mr-3" />
      {{ t('home.commitments.delivery.title') }}
    </div>
    <div class="d-flex align-center text-body-2">
      <v-icon icon="mdi-swap-horizontal" color="primary" class="mr-3" />
      {{ t('home.commitments.returns.title') }}
    </div>
    <div class="d-flex align-center text-body-2">
      <v-icon icon="mdi-shield-lock-outline" color="primary" class="mr-3" />
      {{ t('home.commitments.payment.title') }}
    </div>
  </div>
</template>
