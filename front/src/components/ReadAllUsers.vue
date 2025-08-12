<script setup lang="ts">
import { onMounted } from 'vue'
import { useUsersStore } from '../stores/users'
const usersStore = useUsersStore()

onMounted(() => {
  usersStore.fetchUsers()
})
</script>

<template>
  <table>
    <tr v-for="user in usersStore.users" :key="user.id">
      <td>{{ user.id }}</td>
      <td>{{ user.name }}</td>
      <td>{{ user.email }}</td>
    </tr>
  </table>
</template>