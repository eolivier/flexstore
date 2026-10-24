<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { formatPrice } from '@/utils/price';

  defineProps<{ order: JsonOrder }>();

  const { t, locale } = useI18n();
</script>

<template>
  <v-card>
    <v-table class="lines">
      <thead>
        <tr>
          <th>{{ t('orders.product') }}</th>
          <th class="text-right d-none d-sm-table-cell">{{ t('orders.unitPrice') }}</th>
          <th class="text-right">{{ t('orders.quantity') }}</th>
          <th class="text-right">{{ t('orders.lineTotal') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="line in order.lines" :key="line.productId">
          <td>
            <router-link :to="`/products/${line.productId}`" class="product-link text-high-emphasis font-weight-medium">
              {{ line.productName }}
            </router-link>
          </td>
          <td class="text-right text-no-wrap d-none d-sm-table-cell text-medium-emphasis">
            {{ formatPrice(line.unitPrice, order.currency, locale) }}
          </td>
          <td class="text-right">{{ line.quantity }}</td>
          <td class="text-right text-no-wrap font-weight-medium">{{ formatPrice(line.linePrice, order.currency, locale) }}</td>
        </tr>
      </tbody>
    </v-table>
    <v-divider />
    <div class="d-flex align-center pa-5">
      <span class="text-subtitle-1 font-weight-bold">{{ t('orders.total') }}</span>
      <v-spacer />
      <span class="text-h5 font-weight-bold">{{ formatPrice(order.totalPrice, order.currency, locale) }}</span>
    </div>
  </v-card>
</template>

<style scoped>
  .lines {
    background: transparent;
  }
  .product-link {
    text-decoration: none;
  }
  .product-link:hover {
    text-decoration: underline;
  }
</style>
