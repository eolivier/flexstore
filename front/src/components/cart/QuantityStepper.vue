<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  const props = defineProps<{ modelValue: number }>();
  const emit = defineEmits<{ (e: 'update:modelValue', value: number): void }>();

  const { t } = useI18n();

  function update(value: number) {
    const quantity = Number.isFinite(value) ? Math.max(0, Math.floor(value)) : props.modelValue;
    if (quantity !== props.modelValue) {
      emit('update:modelValue', quantity);
    }
  }

  function onInput(event: Event) {
    const input = event.target as HTMLInputElement;
    update(input.valueAsNumber);
    // Show the accepted value again when the typed one is invalid or unchanged
    input.value = String(props.modelValue);
  }
</script>

<template>
  <div class="stepper d-inline-flex align-center">
    <v-btn
      :icon="modelValue <= 1 ? 'mdi-trash-can-outline' : 'mdi-minus'"
      :aria-label="modelValue <= 1 ? t('cart.remove') : t('cart.decrease')"
      size="x-small"
      variant="text"
      @click="update(modelValue - 1)"
    />
    <input
      class="stepper-input"
      type="number"
      min="0"
      inputmode="numeric"
      :value="modelValue"
      :aria-label="t('table.quantity')"
      @change="onInput"
      @keydown.enter="($event.target as HTMLInputElement).blur()"
    />
    <v-btn
      icon="mdi-plus"
      :aria-label="t('cart.increase')"
      size="x-small"
      variant="text"
      @click="update(modelValue + 1)"
    />
  </div>
</template>

<style scoped>
  .stepper {
    height: 34px;
    padding: 0 2px;
    border: 1px solid rgba(var(--v-border-color), 0.18);
    border-radius: 999px;
    background: rgb(var(--v-theme-surface));
  }
  .stepper-input {
    width: 34px;
    text-align: center;
    font-weight: 600;
    font-size: 0.875rem;
    border: none;
    outline: none;
    background: transparent;
    color: inherit;
    -moz-appearance: textfield;
    appearance: textfield;
  }
  .stepper-input::-webkit-outer-spin-button,
  .stepper-input::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  .stepper:focus-within {
    border-color: rgb(var(--v-theme-primary));
  }
</style>
