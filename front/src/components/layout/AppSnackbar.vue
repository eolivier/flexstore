<script setup lang="ts">
  import { useSnackbar } from '@/composables/useSnackbar';

  const { message, open } = useSnackbar();
</script>

<template>
  <v-snackbar
    v-if="message"
    :key="message.id"
    v-model="open"
    :color="message.color"
    :timeout="message.timeout"
    location="bottom right"
  >
    <v-icon v-if="message.icon" :icon="message.icon" class="mr-2" />
    {{ message.text }}
    <template v-if="message.action" #actions>
      <v-btn :to="message.action.to" variant="text" color="secondary">{{ message.action.text }}</v-btn>
    </template>
  </v-snackbar>
</template>
