<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <h1 class="text-h5 font-weight-bold mb-6">Electronics Catalog</h1>

        <v-data-table :headers="headers" :items="electronics" class="elevation-2">
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
    { title: 'Name', key: 'name' },
    { title: 'Description', key: 'description' },
    { title: 'Price', key: 'price', align: 'end' },
  ];

  const electronics = [
    {
      name: '5G Smartphone',
      description: 'OLED display, 128 GB',
      price: '399€',
    },
    {
      name: '10" Tablet',
      description: 'Ideal for multimedia',
      price: '249€',
    },
    {
      name: 'Laptop',
      description: '512 GB SSD, 16 GB RAM',
      price: '799€',
    },
  ];
</script>
