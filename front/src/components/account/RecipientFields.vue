<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Recipient } from '@/models/account';
  import UserNameFields from './UserNameFields.vue';

  // Who receives what is sent to an address: a person by their names (required),
  // or a company by its name and VAT number, with the names of a contact for a delivery (optional)
  const recipient = defineModel<Recipient>({ required: true });
  defineProps<{ purpose: 'delivery' | 'billing' }>();

  const { t } = useI18n();

  const required = (v: string) => !!v?.trim() || t('createUser.mandatoryField');

  const isCompany = computed({
    get: () => recipient.value.recipientType === 'LEGAL_ENTITY',
    set: (company: boolean) => {
      recipient.value.recipientType = company ? 'LEGAL_ENTITY' : 'PRIVATE_INDIVIDUAL';
    },
  });
</script>

<template>
  <v-checkbox v-model="isCompany" :label="t('checkout.iAmACompany')" hide-details class="mb-2" />
  <template v-if="isCompany">
    <v-text-field
      v-model="recipient.companyName"
      :label="t('checkout.companyName')"
      :rules="[required]"
      prepend-inner-icon="mdi-domain"
      autocomplete="organization"
      class="mb-2"
      required
    />
    <v-text-field v-model="recipient.vatNumber" :label="t('checkout.vatNumber')" class="mb-2" />
    <UserNameFields
      v-if="purpose === 'delivery'"
      v-model:first-name="recipient.firstName"
      v-model:last-name="recipient.lastName"
      requirement="optional"
    />
  </template>
  <UserNameFields v-else v-model:first-name="recipient.firstName" v-model:last-name="recipient.lastName" />
</template>
