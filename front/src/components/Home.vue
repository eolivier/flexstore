<script setup lang="ts">
import {ref, watch} from 'vue'
import { useCounterStore } from '../stores/counter'
import ReadAllUsers from "./ReadAllUsers.vue";
import CreateUser from "./CreateUser.vue";

defineProps<{ msg: string }>()

const counter = useCounterStore()
const inputValue = ref(counter.count)

// Synchronise la variable locale avec le store si besoin
watch(() => counter.count, (val) => {
  inputValue.value = val
})

// Mets à jour le store à chaque changement de l’input
watch(inputValue, (val) => {
  counter.count = Number(val)
})
</script>

<template>
  <h1>{{ msg }}</h1>

  <div class="card">
    <button @click="counter.increment">Compteur: {{ counter.count }}</button>
    <input v-model="inputValue" />
    <CreateUser />
    <ReadAllUsers />
  </div>

</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
