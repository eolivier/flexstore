<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <h1 class="text-h5 font-weight-bold mb-6">Accessories Catalog</h1>

        <v-data-table
            :headers="headers"
            :items="accessories"
            class="elevation-2"
        >
          <template #item.price="{ item }">
            <strong>{{ item.price }}</strong>
          </template>
        </v-data-table>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
const headers = [
  { title: "Name", key: "name" },
  { title: "Description", key: "description" },
  { title: "Price", key: "price", align: "end" },
]

const accessories = [
  {
    name: "Smartwatch",
    description: "Activity tracking and notifications",
    price: "99€",
  },
  {
    name: "Bluetooth Headphones",
    description: "High-quality sound, 20h battery life",
    price: "59€",
  },
  {
    name: "Urban Backpack",
    description: "Laptop compartment, modern design",
    price: "39€",
  },
]
</script>
