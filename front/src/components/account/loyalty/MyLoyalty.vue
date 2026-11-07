<script setup lang="ts">
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useLoyaltyStore } from '@/stores/loyalty.store';

  // The loyalty points of the signed-in customer: 1 point for each euro or dollar spent
  const { t } = useI18n();
  const loyaltyStore = useLoyaltyStore();

  onMounted(() => {
    loyaltyStore.fetchPoints();
  });
</script>

<template>
  <v-container class="py-10">
    <h1 class="text-h4 font-weight-bold">{{ t('loyalty.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mt-1 mb-6">{{ t('loyalty.subtitle') }}</p>

    <v-alert v-if="loyaltyStore.loadError" type="error" variant="tonal" class="mb-4">
      {{ t('loyalty.loadError') }}
    </v-alert>

    <v-skeleton-loader v-else-if="loyaltyStore.loading" type="article" class="rounded-xl" />

    <v-card v-else class="pa-8 text-center">
      <v-icon icon="mdi-star-circle-outline" size="48" color="primary" class="mb-3" />
      <p class="text-body-2 text-medium-emphasis">{{ t('loyalty.balance') }}</p>
      <p class="text-h3 font-weight-bold" data-testid="loyalty-balance">
        {{ t('loyalty.points', loyaltyStore.points) }}
      </p>
      <p class="text-body-2 text-medium-emphasis mt-4">{{ t('loyalty.rule') }}</p>
    </v-card>
  </v-container>
</template>
