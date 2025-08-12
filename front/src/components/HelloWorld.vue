<script setup lang="ts">
import {ref, watch} from 'vue'
import { useCounterStore } from '../stores/counter'
import { createUser } from './CreateUser.ts'

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
    <button @click="createUser">Créer utilisateur</button>
  </div>

  <p>
    Check out
    <a href="https://vuejs.org/guide/quick-start.html#local" target="_blank"
      >create-vue</a
    >, the official Vue + Vite starter
  </p>
  <p>
    Learn more about IDE Support for Vue in the
    <a
      href="https://vuejs.org/guide/scaling-up/tooling.html#ide-support"
      target="_blank"
      >Vue Docs Scaling up Guide</a
    >.
  </p>
  <p class="read-the-docs">Click on the Vite and Vue logos to learn more</p>
</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
