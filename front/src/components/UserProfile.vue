<template>
  <v-app>
    <v-main>
      <v-container class="py-10">
        <v-card class="mx-auto" max-width="400" elevation="4">
          <v-card-title class="text-h6 font-weight-bold">
            User Profile
          </v-card-title>
          <v-divider />

          <v-card-text>
            <v-list density="compact">
              <v-list-item>
                <v-list-item-title>Name:</v-list-item-title>
                <v-list-item-subtitle>{{ user.name }}</v-list-item-subtitle>
              </v-list-item>

              <v-list-item>
                <v-list-item-title>Email:</v-list-item-title>
                <v-list-item-subtitle>{{ user.email }}</v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
const user = {
  name: "John Doe",
  email: "john.doe@example.com"
}
</script>
