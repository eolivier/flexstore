<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { isValidSiren, isValidVatNumber } from '@/utils/company';

  // The company a customer buys for: its name and its SIREN are required, its intra-community VAT number is
  // optional
  const companyName = defineModel<string>('companyName', { required: true });
  const siren = defineModel<string>('siren', { required: true });
  const vatNumber = defineModel<string>('vatNumber', { required: true });

  const { t } = useI18n();

  const rules = {
    required: (v: string) => !!v?.trim() || t('createUser.mandatoryField'),
    siren: (v: string) => isValidSiren(v) || t('company.invalidSiren'),
    vatNumber: (v: string) => isValidVatNumber(v, siren.value) || t('company.invalidVatNumber'),
  };
</script>

<template>
  <v-text-field
    v-model="companyName"
    :label="t('company.name')"
    :rules="[rules.required]"
    prepend-inner-icon="mdi-domain"
    autocomplete="organization"
    class="mb-2"
    data-test="company-name"
    required
  />
  <v-row dense>
    <v-col cols="12" sm="6">
      <v-text-field
        v-model="siren"
        :label="t('company.siren')"
        :rules="[rules.required, rules.siren]"
        :hint="t('company.sirenHint')"
        class="mb-2"
        data-test="siren"
        required
      />
    </v-col>
    <v-col cols="12" sm="6">
      <v-text-field
        v-model="vatNumber"
        :label="t('company.vatNumber')"
        :rules="[rules.vatNumber]"
        :hint="t('company.vatNumberHint')"
        class="mb-2"
        data-test="vat-number"
      />
    </v-col>
  </v-row>
</template>
