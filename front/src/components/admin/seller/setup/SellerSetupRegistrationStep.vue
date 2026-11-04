<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useSellerFieldRules } from './useSellerFieldRules';

  const siret = defineModel<string>('siret', { required: true });
  const registryCity = defineModel<string>('registryCity', { required: true });
  const vatNumber = defineModel<string>('vatNumber', { required: true });

  const { t } = useI18n();
  const rules = useSellerFieldRules();
</script>

<template>
  <div>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t('sellerSetup.registrationText') }}</p>
    <v-text-field
      v-model="siret"
      :label="t('seller.siret')"
      :hint="t('seller.siretHint')"
      :rules="[rules.siret]"
      class="mb-2"
    />
    <v-text-field
      v-model="registryCity"
      :label="t('seller.registryCity')"
      :hint="t('seller.registryCityHint')"
      :rules="[rules.required]"
      class="mb-2"
    />
    <v-text-field
      v-model="vatNumber"
      :label="t('seller.vatNumber')"
      :hint="t('seller.vatNumberHint')"
      :rules="[rules.vatNumber]"
    />
  </div>
</template>
