<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <h1 class="text-h5 font-weight-bold mb-6">Clothes Catalog</h1>

        <v-data-table
            :headers="headers"
            :items="clothes"
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

const clothes = [
  {
    name: "Organic Cotton T-shirt",
    description: "Comfortable and eco-friendly",
    price: "25€",
  },
  {
    name: "Waterproof Jacket",
    description: "Resists rain and wind",
    price: "79€",
  },
  {
    name: "Slim Jeans",
    description: "Modern fit, stretch",
    price: "49€",
  },
]
</script>
