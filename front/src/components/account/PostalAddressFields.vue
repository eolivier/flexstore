<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { PostalAddress } from '@/models/account';

  // The fields of a postal address: everything is required but the complement
  const address = defineModel<PostalAddress>({ required: true });

  const { t } = useI18n();

  const required = (v: string) => !!v?.trim() || t('createUser.mandatoryField');
</script>

<template>
  <fieldset class="address-field mb-2">
    <legend class="text-subtitle-2 font-weight-bold mb-2">{{ t('signUp.address') }}</legend>
    <v-text-field
      v-model="address.street"
      :label="t('signUp.street')"
      :rules="[required]"
      prepend-inner-icon="mdi-home-outline"
      autocomplete="address-line1"
      class="mb-2"
      required
    />
    <v-text-field v-model="address.complement" :label="t('signUp.complement')" autocomplete="address-line2" class="mb-2" />
    <v-row dense>
      <v-col cols="12" sm="4">
        <v-text-field v-model="address.postalCode" :label="t('signUp.postalCode')" :rules="[required]" autocomplete="postal-code" required />
      </v-col>
      <v-col cols="12" sm="8">
        <v-text-field v-model="address.city" :label="t('signUp.city')" :rules="[required]" autocomplete="address-level2" required />
      </v-col>
    </v-row>
    <v-text-field v-model="address.country" :label="t('signUp.country')" :rules="[required]" autocomplete="country-name" required />
  </fieldset>
</template>

<style scoped>
  .address-field {
    border: none;
  }
</style>
