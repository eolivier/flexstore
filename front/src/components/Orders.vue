<template>
  <v-container class="py-10">
    <h1 class="text-h5 font-weight-bold mb-6">{{ t('orders.title') }}</h1>

    <v-data-table :headers="headers" :items="orders" class="elevation-2" :items-per-page="5">
      <template #item.total="{ item }">
        <strong>{{ item.total }}</strong>
      </template>
    </v-data-table>
  </v-container>
</template>

<script setup>
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();

  const headers = computed(() => [
    { title: t('table.orderId'), key: 'id' },
    { title: t('table.customer'), key: 'customer' },
    { title: t('table.items'), key: 'items' },
    { title: t('table.total'), key: 'total', align: 'end' },
    { title: t('table.status'), key: 'status' },
  ]);

  const orders = [
    {
      id: 'ORD-001',
      customer: 'John Doe',
      items: 'Organic Cotton T-shirt, Bluetooth Headphones',
      total: '84€',
      status: 'Shipped',
    },
    {
      id: 'ORD-002',
      customer: 'Alice Smith',
      items: 'Waterproof Jacket, Smartwatch',
      total: '178€',
      status: 'Processing',
    },
    {
      id: 'ORD-003',
      customer: 'Michael Brown',
      items: 'Slim Jeans, 5G Smartphone',
      total: '448€',
      status: 'Delivered',
    },
    {
      id: 'ORD-004',
      customer: 'Emma Wilson',
      items: 'Laptop, Urban Backpack',
      total: '838€',
      status: 'Pending Payment',
    },
    {
      id: 'ORD-005',
      customer: 'David Johnson',
      items: '10" Tablet, Organic Cotton T-shirt',
      total: '274€',
      status: 'Shipped',
    },
  ];
</script>
