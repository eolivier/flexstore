<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useFieldRules } from '@/composables/useFieldRules';
  import type { JsonLegalPage } from '@/generated';
  import { useLegalPagesStore, type LegalPageKind, type LegalPagePublication } from '@/stores/legalPages.store';

  const props = defineProps<{ kind: LegalPageKind; legalPage: JsonLegalPage }>();

  const MAXIMUM_LENGTH = 50000;

  const { t } = useI18n();
  const legalPagesStore = useLegalPagesStore();

  const text = ref(props.legalPage.text);
  const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null);
  // null until every field is checked
  const valid = ref<boolean | null>(true);
  const saving = ref(false);
  // null until published
  const publication = ref<LegalPagePublication | null>(null);

  const rules = {
    ...useFieldRules(),
    maximumLength: (v: string) => v.length <= MAXIMUM_LENGTH || t('legalPages.tooLong'),
  };

  // The text is checked again before publishing: an untouched empty page is not checked yet
  async function publish() {
    const validation = await formRef.value?.validate();
    if (!validation?.valid) return;
    saving.value = true;
    publication.value = await legalPagesStore.publishLegalPage(props.kind, text.value);
    saving.value = false;
  }
</script>

<template>
  <v-form ref="formRef" v-model="valid" @submit.prevent="publish">
    <v-textarea
      v-model="text"
      :label="t(`legalPages.${kind}.title`)"
      :hint="t('legalPages.textHint')"
      :rules="[rules.required, rules.maximumLength]"
      :counter="MAXIMUM_LENGTH"
      rows="12"
      auto-grow
      class="mb-2"
    />
    <div class="d-flex align-center ga-4 mt-2">
      <v-btn type="submit" color="secondary" :loading="saving" :disabled="valid === false">{{
        t('legalPages.publish')
      }}</v-btn>
      <router-link :to="`/${kind}`" class="text-body-2">{{ t('legalPages.see') }}</router-link>
      <span v-if="publication === 'saved'" class="text-body-2 text-success">{{ t('legalPages.saved') }}</span>
      <span v-else-if="publication === 'invalid'" class="text-body-2 text-error">{{
        t('legalPages.invalid')
      }}</span>
      <span v-else-if="publication === 'failed'" class="text-body-2 text-error">{{
        t('legalPages.saveError')
      }}</span>
    </div>
  </v-form>
</template>
