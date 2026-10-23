<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineProps<{
  modelValue: boolean,
  title?: string,
  message?: string
}>();
const emit = defineEmits(['update:modelValue', 'confirm']);

const model = ref(props.modelValue);

watch(() => props.modelValue, (val) => model.value = val);

function close() {
  emit('update:modelValue', false);
}

function confirm() {
  emit('confirm');
  close();
}
</script>

<template>
  <v-dialog v-model="model" max-width="420" @update:model-value="value => !value && close()">
    <v-card class="pa-2">
      <v-card-title class="text-h6 font-weight-bold">{{ title }}</v-card-title>
      <v-card-text class="text-body-2 text-medium-emphasis">{{ message }}</v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" @click="close">{{ t('cancel') }}</v-btn>
        <v-btn color="error" variant="flat" @click="confirm">{{ t('confirm') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>