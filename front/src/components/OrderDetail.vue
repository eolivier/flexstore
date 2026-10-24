<script setup lang="ts">
  import { computed, onMounted, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import { useOrdersStore, type OrderLookup } from '@/stores/orders.store';
  import { formatPrice } from '@/utils/price';
  import { orderNumber, orderStatusColors, placedOn } from '@/utils/order';

  const props = defineProps<{ id: string }>();

  const { t, locale } = useI18n();
  const route = useRoute();
  const ordersStore = useOrdersStore();

  const orderLookup = ref<OrderLookup | null>(null);
  const order = computed(() => (orderLookup.value?.status === 'found' ? orderLookup.value.order : null));
  // Set when the customer has just placed this order from the cart
  const justPlaced = computed(() => route.query.placed === 'true');
  const itemsCount = computed(() => order.value?.lines.reduce((count, line) => count + line.quantity, 0) ?? 0);

  async function load() {
    orderLookup.value = null;
    orderLookup.value = await ordersStore.fetchOrder(props.id);
  }

  onMounted(load);
  watch(() => props.id, load);
</script>

<template>
  <v-container class="page py-10">
    <v-btn to="/orders" variant="text" prepend-icon="mdi-arrow-left" class="mb-4 px-2">
      {{ t('orders.backToOrders') }}
    </v-btn>

    <v-skeleton-loader v-if="!orderLookup" type="heading, list-item-two-line@3" class="rounded-xl" />

    <v-card v-else-if="orderLookup.status === 'notFound'" class="pa-10 text-center">
      <v-icon icon="mdi-package-variant-remove" size="56" class="text-medium-emphasis mb-3" />
      <h1 class="text-h6 font-weight-bold mb-1">{{ t('orders.notFound') }}</h1>
      <p class="text-body-2 text-medium-emphasis mb-6">{{ t('orders.notFoundText') }}</p>
      <v-btn to="/orders" color="secondary">{{ t('orders.backToOrders') }}</v-btn>
    </v-card>

    <v-alert v-else-if="orderLookup.status === 'error'" type="error" variant="tonal">
      {{ t('orders.orderLoadError') }}
    </v-alert>

    <template v-else-if="order">
      <v-alert v-if="justPlaced" type="success" variant="tonal" class="mb-6" icon="mdi-check-circle-outline">
        {{ t('orders.placed') }}
      </v-alert>

      <div class="d-flex flex-wrap align-center ga-3 mb-6">
        <div class="flex-grow-1">
          <h1 class="text-h4 font-weight-bold">{{ t('orders.number', { number: orderNumber(order) }) }}</h1>
          <p class="text-body-2 text-medium-emphasis mt-1">
            {{ t('orders.placedOn', { date: placedOn(order, locale) }) }} · {{ t('orders.itemsCount', itemsCount) }}
          </p>
        </div>
        <v-chip :color="orderStatusColors[order.status]" variant="tonal" class="font-weight-medium">
          {{ t(`orders.status.${order.status}`) }}
        </v-chip>
      </div>

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
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
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
