<script setup lang="ts">
import {useUsersStore} from "../stores/users.ts";
import {ref} from "vue";

const usersStore = useUsersStore()

const name = ref('')
const email = ref('')

async function createUser() {
  const newUser = {
    id: Date.now(), // ou utilise uuid si besoin
    name: name.value,
    email: email.value
  }
  await usersStore.addUser(newUser)
  name.value = ''
  email.value = ''
}
</script>

<template>
  <form @submit.prevent="createUser">
    <label>
      Nom
      <input v-model="name" placeholder="Nom" />
    </label>
    <label>
      Email
      <input v-model="email" placeholder="Email" />
    </label>
    <button type="submit">Créer utilisateur</button>
  </form>
</template>
