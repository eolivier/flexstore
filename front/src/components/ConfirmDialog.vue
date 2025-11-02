<script setup lang="ts">
import { defineProps, defineEmits, ref, watch } from 'vue';
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
  <v-dialog v-model="model" max-width="400">
    <v-card>
      <v-card-title>{{ title }}</v-card-title>
      <v-card-text>{{ message }}</v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn color="grey" @click="close">{{ t('cancel') }}</v-btn>
        <v-btn color="red" @click="confirm">{{ t('confirm') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>