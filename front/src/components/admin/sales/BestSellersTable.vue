<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonBestSeller } from '@/generated';
  import { formatPrice } from '@/utils/price';

  // The products sold the most in the period, the best first
  defineProps<{ bestSellers: JsonBestSeller[]; currency: string }>();

  const { t, locale } = useI18n();
</script>

<template>
  <v-card class="pa-5">
    <h2 class="text-h6 font-weight-bold mb-2">{{ t('salesDashboard.bestSellers') }}</h2>
    <p v-if="bestSellers.length === 0" class="text-body-2 text-medium-emphasis">
      {{ t('salesDashboard.noSale') }}
    </p>
    <v-table v-else density="comfortable">
      <thead>
        <tr>
          <th>{{ t('salesDashboard.product') }}</th>
          <th class="text-end">{{ t('salesDashboard.unitsSold') }}</th>
          <th class="text-end">{{ t('salesDashboard.revenue') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="bestSeller in bestSellers" :key="bestSeller.productId" data-test="best-seller">
          <td>{{ bestSeller.productName }}</td>
          <td class="text-end">{{ bestSeller.unitsSold }}</td>
          <td class="text-end">{{ formatPrice(bestSeller.revenue, currency, locale) }}</td>
        </tr>
      </tbody>
    </v-table>
  </v-card>
</template>
