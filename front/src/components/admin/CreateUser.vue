<script setup>
  import { useUsersStore } from '@/stores/users.store.ts';
  import { nextTick, ref } from 'vue';
  import { useI18n } from 'vue-i18n';

  const name = ref('');
  const email = ref('');
  const password = ref('');
  const valid = ref(false);
  const formRef = ref(null);

  const usersStore = useUsersStore();
  const { t } = useI18n();

  const rules = {
    required: (v) => !!v || t('createUser.mandatoryField'),
    email: (v) => /.+@.+\..+/.test(v) || t('createUser.invalidEmail'),
    passwordLength: (v) => v.length >= 8 || t('createUser.passwordTooShort'),
  };

  const createUser = async (name, email, password) => {
    const newUser = { name, email, password };
    await usersStore.addUser(newUser);
    resetForm();
  };

  const submitForm = () => {
    if (valid.value) {
      createUser(name.value, email.value, password.value);
    }
  };

  const resetForm = () => {
    name.value = '';
    email.value = '';
    password.value = '';
    valid.value = false;
    nextTick(() => {
      if (formRef.value) {
        formRef.value.resetValidation();
      }
    });
  };
</script>

<template>
  <v-container class="pa-4" max-width="400">
    <h1 class="text-h5 font-weight-bold mb-6">{{ t('createUser.label') }}</h1>
    <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
      <v-text-field
        v-model="name"
        :label="t('createUser.name')"
        :rules="[rules.required]"
        required
      />

      <v-text-field
        v-model="email"
        :label="t('createUser.email')"
        :rules="[rules.required, rules.email]"
        type="email"
        required
      />

      <v-text-field
        v-model="password"
        :label="t('createUser.password')"
        :rules="[rules.required, rules.passwordLength]"
        type="password"
        required
      />

      <v-btn type="submit" color="primary" class="mt-2">{{ t('createUser.label') }}</v-btn>
    </v-form>
  </v-container>
</template>
