<script setup lang="ts">
  import EmptyState from '@/components/layout/EmptyState.vue';
  import LoadError from '@/components/layout/LoadError.vue';
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useOrdersStore } from '@/stores/orders.store';
  import ListPager from '@/components/layout/ListPager.vue';
  import OrderSummaryCard from './OrderSummaryCard.vue';
  import AccountBreadcrumbs from '@/components/account/AccountBreadcrumbs.vue';

  const { t } = useI18n();
  const ordersStore = useOrdersStore();

  onMounted(() => {
    ordersStore.fetchOrders();
  });
</script>

<template>
  <v-container class="py-10">
    <AccountBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('orders.title') }}</h1>

    <LoadError v-if="ordersStore.loadError" :message="t('orders.loadError')" class="mb-6" />

    <v-skeleton-loader v-else-if="!ordersStore.orders" type="list-item-avatar-two-line@3" class="rounded-xl" />

    <EmptyState
      v-else-if="ordersStore.orders.length === 0"
      icon="mdi-package-variant"
      :message="t('orders.empty')"
      :description="t('orders.emptyText')"
    >
      <v-btn to="/products" color="secondary" size="large" append-icon="mdi-arrow-right">
        {{ t('home.heroButton') }}
      </v-btn>
    </EmptyState>

    <template v-else>
      <div class="d-flex flex-column ga-4">
        <OrderSummaryCard v-for="order in ordersStore.orders" :key="order.id" :order="order" />
      </div>
      <ListPager
        :model-value="ordersStore.page"
        :total="ordersStore.total"
        @update:model-value="ordersStore.fetchOrders"
      />
    </template>
  </v-container>
</template>
