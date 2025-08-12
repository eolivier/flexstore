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
</script>

<template>
  <table>
    <tr v-for="user in usersStore.users" :key="user.id">
      <td>{{ user.id }}</td>
      <td>{{ user.name }}</td>
      <td>{{ user.email }}</td>
      <td>
        <button @click="handleDelete(user.id)">Supprimer</button>
      </td>
    </tr>
  </table>
</template>