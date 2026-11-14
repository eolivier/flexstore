<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // A modal dialog to edit or confirm something quickly: a title, an explanation, the content, then the buttons.
  // "Cancel" comes first, built in; the actions given by the screen follow, the main one last
  withDefaults(defineProps<{ title: string; text?: string; maxWidth?: number }>(), { text: '', maxWidth: 480 });
  const open = defineModel<boolean>({ required: true });
  // Whether the fields of the form are valid, for the screen whose save button waits for them (null until checked)
  const valid = defineModel<boolean | null>('valid', { default: null });
  const emit = defineEmits<{ submit: [] }>();

  const { t } = useI18n();
</script>

<template>
  <v-dialog v-model="open" :max-width="maxWidth">
    <v-card class="pa-6">
      <h2 class="text-h6 font-weight-bold" :class="text ? 'mb-1' : 'mb-4'">{{ title }}</h2>
      <p v-if="text" class="text-body-2 text-medium-emphasis mb-4">{{ text }}</p>
      <v-form v-model="valid" @submit.prevent="emit('submit')">
        <slot />
        <div v-if="$slots.actions" class="d-flex flex-wrap justify-end ga-2">
          <v-btn variant="text" data-test="dialog-cancel" @click="open = false">{{ t('cancel') }}</v-btn>
          <slot name="actions" />
        </div>
      </v-form>
    </v-card>
  </v-dialog>
</template>
