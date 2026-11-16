<script setup lang="ts">
  import { nextTick, ref, watch } from 'vue';
  import { formattedTwoFactorCode, twoFactorCodeDigits } from '@/utils/twoFactorCode';

  // The model is the digits only; the field shows them as "123 456"
  const digits = defineModel<string>({ required: true });

  const displayedCode = ref(formattedTwoFactorCode(digits.value));

  watch(digits, (newDigits) => {
    displayedCode.value = formattedTwoFactorCode(newDigits);
  });

  async function type(typedCode: string) {
    digits.value = twoFactorCodeDigits(typedCode);
    // Shown as typed for a moment, so that a character dropped (a letter) is erased from the field too
    displayedCode.value = typedCode;
    await nextTick();
    displayedCode.value = formattedTwoFactorCode(digits.value);
  }
</script>

<template>
  <v-text-field
    :model-value="displayedCode"
    inputmode="numeric"
    autocomplete="one-time-code"
    maxlength="7"
    placeholder="123 456"
    @update:model-value="type"
  />
</template>
