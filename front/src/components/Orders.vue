<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <h1 class="text-h5 font-weight-bold mb-6">Orders</h1>

        <v-data-table
            :headers="headers"
            :items="orders"
            class="elevation-2"
            :items-per-page="5"
        >
          <template #item.total="{ item }">
            <strong>{{ item.total }}</strong>
          </template>
        </v-data-table>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
const headers = [
  { title: "Order ID", key: "id" },
  { title: "Customer", key: "customer" },
  { title: "Items", key: "items" },
  { title: "Total", key: "total", align: "end" },
  { title: "Status", key: "status" },
]

const orders = [
  {
    id: "ORD-001",
    customer: "John Doe",
    items: "Organic Cotton T-shirt, Bluetooth Headphones",
    total: "84€",
    status: "Shipped",
  },
  {
    id: "ORD-002",
    customer: "Alice Smith",
    items: "Waterproof Jacket, Smartwatch",
    total: "178€",
    status: "Processing",
  },
  {
    id: "ORD-003",
    customer: "Michael Brown",
    items: "Slim Jeans, 5G Smartphone",
    total: "448€",
    status: "Delivered",
  },
  {
    id: "ORD-004",
    customer: "Emma Wilson",
    items: "Laptop, Urban Backpack",
    total: "838€",
    status: "Pending Payment",
  },
  {
    id: "ORD-005",
    customer: "David Johnson",
    items: "10\" Tablet, Organic Cotton T-shirt",
    total: "274€",
    status: "Shipped",
  },
]
</script>
