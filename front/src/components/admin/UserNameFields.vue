<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // The first name and the last name of a user, side by side on a large screen
  const firstName = defineModel<string>('firstName', { required: true });
  const lastName = defineModel<string>('lastName', { required: true });

  const { t } = useI18n();

  const required = (v: string) => !!v?.trim() || t('createUser.mandatoryField');
</script>

<template>
  <v-row dense>
    <v-col cols="12" sm="6">
      <v-text-field
        v-model="firstName"
        :label="t('createUser.firstName')"
        :rules="[required]"
        prepend-inner-icon="mdi-account-outline"
        autocomplete="given-name"
        class="mb-2"
        required
      />
    </v-col>
    <v-col cols="12" sm="6">
      <v-text-field
        v-model="lastName"
        :label="t('createUser.lastName')"
        :rules="[required]"
        autocomplete="family-name"
        class="mb-2"
        required
      />
    </v-col>
  </v-row>
</template>
