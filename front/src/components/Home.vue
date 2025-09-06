<script setup lang="ts">
import ReadAllUsers from "./ReadAllUsers.vue";
import CreateUser from "./CreateUser.vue";

defineProps<{ msg: string }>()

</script>

<template>
  <h1>{{ msg }}</h1>

  <div class="card">
    <CreateUser />
    <ReadAllUsers />
  </div>

</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
