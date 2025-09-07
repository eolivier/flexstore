<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <h1 class="text-h5 font-weight-bold mb-6">My Cart</h1>

        <v-data-table
            :headers="headers"
            :items="cartItems"
            class="elevation-2"
            hide-default-footer
        >
          <template #item.subtotal="{ item }">
            <strong>{{ item.subtotal }}</strong>
          </template>

          <!-- Custom footer for total -->
          <template #bottom>
            <v-divider />
            <div class="d-flex justify-end pa-4">
              <strong class="text-h6">Total: {{ total }}</strong>
            </div>
          </template>
        </v-data-table>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
const headers = [
  { title: "Product", key: "name" },
  { title: "Details", key: "details" },
  { title: "Subtotal", key: "subtotal", align: "end" },
]

const cartItems = [
  {
    name: "T-shirt",
    details: "2 × 20€",
    subtotal: "40€",
  },
  {
    name: "Headphones",
    details: "1 × 50€",
    subtotal: "50€",
  },
  {
    name: "Watch",
    details: "1 × 80€",
    subtotal: "80€",
  },
]

const total = "170€"
</script>
