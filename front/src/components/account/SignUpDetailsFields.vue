<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { CustomerCompany, Location } from '@/models/account';
  import PostalAddressFields from './PostalAddressFields.vue';
  import CompanyFields from './CompanyFields.vue';

  // What a customer may give when they create their account, after the express fields: their phone number,
  // their company and their postal address
  const phoneNumber = defineModel<string>('phoneNumber', { required: true });
  const isCompany = defineModel<boolean>('isCompany', { required: true });
  const company = defineModel<CustomerCompany>('company', { required: true });
  const location = defineModel<Location>('location', { required: true });

  const { t } = useI18n();

  const rules = {
    required: (v: string) => !!v?.trim() || t('createUser.mandatoryField'),
    phoneNumber: (v: string) => /^\+?[0-9][0-9 .()-]{5,19}$/.test(v.trim()) || t('signUp.invalidPhoneNumber'),
  };
</script>

<template>
  <div data-test="sign-up-details">
    <v-text-field
      v-model="phoneNumber"
      :label="t('signUp.phoneNumber')"
      :rules="[rules.required, rules.phoneNumber]"
      type="tel"
      prepend-inner-icon="mdi-phone-outline"
      autocomplete="tel"
      class="mb-4"
      required
    />
    <v-checkbox
      v-model="isCompany"
      :label="t('company.iAmACompany')"
      density="compact"
      hide-details
      class="mb-2"
      data-test="is-company"
    />
    <CompanyFields
      v-if="isCompany"
      v-model:company-name="company.companyName"
      v-model:siren="company.siren"
      v-model:vat-number="company.vatNumber"
    />
    <PostalAddressFields v-model="location" />
  </div>
</template>
