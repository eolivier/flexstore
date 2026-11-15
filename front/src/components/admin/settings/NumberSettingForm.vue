<script setup lang="ts">
  import FormActions from '@/components/form/FormActions.vue';
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useEditedForm } from '@/composables/useEditedForm';

  // A setting of the shop that is one number: the field, "Cancel" and "Save", and what the save answered
  const props = withDefaults(
    defineProps<{
      stored: number;
      label: string;
      rule: (value: number | string) => true | string;
      save: (value: number) => Promise<boolean>;
      min: number;
      max?: number;
      step?: number;
    }>(),
    { max: undefined, step: 1 }
  );

  const { t } = useI18n();

  // the field gives a text: the stored number is compared with it as a text too
  const { editedValues, isModified, markSaved, restore } = useEditedForm({ value: String(props.stored) });
  const valid = ref(true);
  const saving = ref(false);
  // null until saved
  const saved = ref<boolean | null>(null);

  watch(
    () => props.stored,
    (stored) => {
      editedValues.value = String(stored);
      markSaved();
    }
  );

  async function submit() {
    if (!valid.value) return;
    saving.value = true;
    saved.value = await props.save(Number(editedValues.value));
    if (saved.value) markSaved();
    saving.value = false;
  }

  function cancel() {
    restore();
    saved.value = null;
  }
</script>

<template>
  <v-form v-model="valid" class="d-flex align-start ga-3" @submit.prevent="submit">
    <v-text-field
      v-model="editedValues.value"
      type="number"
      :min="min"
      :max="max"
      :step="step"
      :label="label"
      :rules="[rule]"
      density="compact"
      class="number-field"
    />
    <FormActions :loading="saving" :disabled="!valid" :cancel-disabled="!isModified" @cancel="cancel" />
  </v-form>
  <p v-if="saved === false" class="text-body-2 text-error">{{ t('settings.saveError') }}</p>
  <p v-else-if="saved" class="text-body-2 text-success">{{ t('settings.saved') }}</p>
</template>

<style scoped>
  .number-field {
    max-width: 180px;
  }
</style>
