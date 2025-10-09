<script setup lang="ts">
  import { ref } from 'vue';
  const adminOpen = ref(false);
</script>

<template>
  <v-list-group v-model="adminOpen" prepend-icon="mdi-cog">
    <template #activator="{ props }">
      <v-list-item v-bind="props">Administration</v-list-item>
    </template>
    <v-list-item to="/admin/users" prepend-icon="mdi-account-multiple" link>
      Listes des utilisateurs
    </v-list-item>
    <v-list-item to="/admin/create-user" prepend-icon="mdi-account-plus" link>
      Créer un utilisateur
    </v-list-item>
    <v-list-item to="/admin/create-product" prepend-icon="mdi-package-variant" link>
      Créer un produit
    </v-list-item>
  </v-list-group>
</template>
