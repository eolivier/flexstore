<script setup lang="ts">
  import { ref } from 'vue';

  withDefaults(
    defineProps<{ label: string; rules: ((value: string) => true | string)[]; autocomplete?: string }>(),
    { autocomplete: 'new-password' }
  );

  const password = defineModel<string>({ required: true });

  const showPassword = ref(false);
</script>

<template>
  <v-text-field
    v-model="password"
    :label="label"
    :rules="rules"
    :type="showPassword ? 'text' : 'password'"
    prepend-inner-icon="mdi-lock-outline"
    :append-inner-icon="showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
    :autocomplete="autocomplete"
    required
    @click:append-inner="showPassword = !showPassword"
  />
</template>
