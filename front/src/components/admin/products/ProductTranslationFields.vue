<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { DEFAULT_LANGUAGE, SHOP_LANGUAGES, type ProductTexts } from '@/models/product';
  import { isEntered, isHalfEntered } from '@/utils/productTranslations';

  // The name and the description of the product in each language, chosen with its chip. The fields of the
  // other languages stay in the form (hidden), so that the form checks every language
  const texts = defineModel<ProductTexts>({ required: true });

  const { t } = useI18n();
  const shownLanguage = ref(DEFAULT_LANGUAGE);

  // A field shown in error is checked again when the other field of its language changes: emptying the
  // description of a language makes its empty name valid again
  interface CheckedField {
    isValid: boolean | null;
    validate: () => Promise<unknown>;
  }
  const fields = ref<Record<string, CheckedField>>({});
  watch(
    texts,
    () => Object.values(fields.value).forEach((field) => field.isValid === false && field.validate()),
    { deep: true }
  );

  // The default language is required; another one is either empty (not translated) or complete
  function rules(language: string, field: 'name' | 'description') {
    const otherField = field === 'name' ? 'description' : 'name';
    const required = (v: string) => !!v.trim() || t('createProduct.mandatoryField');
    const requiredWithOther = (v: string) =>
      !!v.trim() || !texts.value[language][otherField].trim() || t('createProduct.mandatoryField');
    return [language === DEFAULT_LANGUAGE ? required : requiredWithOther];
  }
</script>

<template>
  <fieldset class="translations mb-2">
    <legend class="text-subtitle-2 mb-1">{{ t('createProduct.translation') }}</legend>
    <!-- The chip of the language shown is filled; a check marks the languages entered -->
    <v-chip-group v-model="shownLanguage" mandatory selected-class="bg-primary" class="mb-2">
      <v-chip
        v-for="language in SHOP_LANGUAGES"
        :key="language"
        :value="language"
        :color="isHalfEntered(texts, language) ? 'error' : undefined"
        variant="outlined"
      >
        {{ language.toUpperCase() }}
        <v-icon v-if="isEntered(texts, language)" icon="mdi-check" size="small" end />
      </v-chip>
    </v-chip-group>
    <div v-for="language in SHOP_LANGUAGES" v-show="language === shownLanguage" :key="language">
      <v-text-field
        :ref="(field) => (fields[`${language}.name`] = field as unknown as CheckedField)"
        v-model="texts[language].name"
        :label="t('createProduct.name')"
        :rules="rules(language, 'name')"
        class="mb-2"
      />
      <v-textarea
        :ref="(field) => (fields[`${language}.description`] = field as unknown as CheckedField)"
        v-model="texts[language].description"
        :label="t('createProduct.description')"
        :rules="rules(language, 'description')"
        variant="outlined"
        rounded="lg"
        rows="3"
        auto-grow
        class="mb-2"
      />
    </div>
  </fieldset>
</template>

<style scoped>
  .translations {
    border: none;
  }
</style>
