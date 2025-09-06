<script setup lang="ts">
import { onMounted } from 'vue'
import { useUsersStore } from '../stores/users'
const usersStore = useUsersStore()

onMounted(() => {
  usersStore.fetchUsers()
})

function handleDelete(id: number) {
  usersStore.deleteUser(id)
}
const headers = [
  { title: 'ID', value: 'id' },
  { title: 'Nom', value: 'name' },
  { title: 'Email', value: 'email' },
  { title: 'Actions', value: 'actions', sortable: false }
]
</script>

<template>
  <v-container>
    <v-data-table
        :headers="headers"
        :items="usersStore.users"
        class="elevation-1"
    >
      <template #item.actions="{ item }">
        <v-btn color="error" @click="handleDelete(item.id)">
          Supprimer
        </v-btn>
      </template>
    </v-data-table>
  </v-container>
</template>