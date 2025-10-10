<script setup>
  import { useUsersStore } from '../../stores/users.ts';
  import { nextTick, ref } from 'vue';
  import { useI18n } from 'vue-i18n';

  const name = ref('');
  const email = ref('');
  const valid = ref(false);
  const formRef = ref(null);

  const usersStore = useUsersStore();
  const { t } = useI18n();

  const rules = {
    required: (v) => !!v || t('createUser.mandatoryField'),
  };

  const createUser = async (name, email) => {
    const newUser = {
      id: Date.now(),
      name: name,
      email: email,
    };
    await usersStore.addUser(newUser);
    resetForm();
  };

  const submitForm = () => {
    if (valid.value) {
      createUser(name.value, email.value);
    }
  };

  const resetForm = () => {
    name.value = '';
    email.value = '';
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
        :rules="[rules.required]"
        required
      />

      <v-btn type="submit" color="primary" class="mt-2">{{ t('createUser.label') }}</v-btn>
    </v-form>
  </v-container>
</template>
