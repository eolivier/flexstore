<script setup lang="ts">
  import FormActions from '@/components/form/FormActions.vue';
  import SaveStatus from '@/components/form/SaveStatus.vue';
  import type { SaveOutcome } from '@/components/form/saveOutcome';
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
  const saved = ref<SaveOutcome | null>(null);

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
    const isSaved = await props.save(Number(editedValues.value));
    saved.value = isSaved ? 'saved' : 'failed';
    if (isSaved) markSaved();
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
  <div v-if="saved">
    <SaveStatus :outcome="saved" :saved="t('settings.saved')" :failed="t('settings.saveError')" />
  </div>
</template>

<style scoped>
  .number-field {
    max-width: 180px;
  }
</style>
