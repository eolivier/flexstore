<script setup lang="ts">
  import EmptyState from '@/components/layout/EmptyState.vue';
  import LoadError from '@/components/layout/LoadError.vue';
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useLoyaltyStore } from '@/stores/loyalty.store';

  function placedOn(placedAt: string, locale: string): string {
    return new Date(placedAt).toLocaleDateString(locale, { day: 'numeric', month: 'long', year: 'numeric' });
  }

  // The loyalty points of the signed-in customer: 1 point for each euro or dollar spent
  const { t, locale } = useI18n();
  const loyaltyStore = useLoyaltyStore();

  onMounted(() => {
    loyaltyStore.fetchPoints();
  });
</script>

<template>
  <v-container class="py-10">
    <h1 class="text-h4 font-weight-bold">{{ t('loyalty.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mt-1 mb-6">{{ t('loyalty.subtitle') }}</p>

    <LoadError v-if="loyaltyStore.loadError" :message="t('loyalty.loadError')" class="mb-4" />

    <v-skeleton-loader v-else-if="loyaltyStore.loading" type="article" class="rounded-xl" />

    <v-card v-else class="pa-8 text-center">
      <v-icon icon="mdi-star-circle-outline" size="48" color="primary" class="mb-3" />
      <p class="text-body-2 text-medium-emphasis">{{ t('loyalty.balance') }}</p>
      <p class="text-h3 font-weight-bold" data-testid="loyalty-balance">
        {{ t('loyalty.points', loyaltyStore.points) }}
      </p>
      <p class="text-body-2 text-medium-emphasis mt-4">{{ t('loyalty.rule') }}</p>
    </v-card>

    <v-card v-if="!loyaltyStore.loadError && !loyaltyStore.loading" class="mt-6">
      <v-card-title>{{ t('loyalty.history') }}</v-card-title>
      <EmptyState
        v-if="loyaltyStore.purchases.length === 0"
        compact
        class="px-4 pb-4"
        :message="t('loyalty.empty')"
        data-testid="loyalty-empty"
      />
      <v-list v-else lines="one">
        <v-list-item
          v-for="purchase in loyaltyStore.purchases"
          :key="purchase.orderNumber"
          data-testid="loyalty-purchase"
          :title="t('loyalty.purchase', { number: purchase.orderNumber })"
          :subtitle="placedOn(purchase.placedAt, locale)"
        >
          <template #append>
            <span class="font-weight-bold">+ {{ t('loyalty.points', purchase.points) }}</span>
          </template>
        </v-list-item>
      </v-list>
    </v-card>
  </v-container>
</template>
