<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { Recipient } from '@/models/account';
  import UserNameFields from './UserNameFields.vue';

  // Who receives what is sent to an address: a first name and a last name, and a company when sent to one
  const recipient = defineModel<Recipient>({ required: true });

  const { t } = useI18n();
</script>

<template>
  <UserNameFields v-model:first-name="recipient.firstName" v-model:last-name="recipient.lastName" />
  <v-text-field
    v-model="recipient.companyName"
    :label="t('checkout.companyName')"
    prepend-inner-icon="mdi-domain"
    autocomplete="organization"
    class="mb-2"
  />
</template>
