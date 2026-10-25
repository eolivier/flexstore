<script setup lang="ts">
  import { useUsersStore } from '@/stores/users.store.ts';
  import { nextTick, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { FullName, type User } from '@/models/user';
  import UserRoleField from './UserRoleField.vue';
  import PasswordField from '@/components/account/PasswordField.vue';
  import UserNameFields from '@/components/account/UserNameFields.vue';

  const firstName = ref('');
  const lastName = ref('');
  const email = ref('');
  const password = ref('');
  // A new user is a simple user unless the administrator chooses otherwise
  const role = ref<NonNullable<User['role']>>('USER');
  const valid = ref(false);
  const saving = ref(false);
  const formRef = ref<{ resetValidation: () => void } | null>(null);
  const feedback = ref({ show: false, success: true, text: '' });

  const usersStore = useUsersStore();
  const { t } = useI18n();

  const rules = {
    required: (v: string) => !!v || t('createUser.mandatoryField'),
    email: (v: string) => /.+@.+\..+/.test(v) || t('createUser.invalidEmail'),
    passwordLength: (v: string) => v.length >= 8 || t('createUser.passwordTooShort'),
  };

  const submitForm = async () => {
    if (!valid.value) return;
    saving.value = true;
    const createdName = FullName.of({ firstName: firstName.value, lastName: lastName.value }).value;
    const created = await usersStore.addUser({
      firstName: firstName.value,
      lastName: lastName.value,
      email: email.value,
      password: password.value,
      role: role.value,
    });
    saving.value = false;
    feedback.value = {
      show: true,
      success: created,
      text: created ? t('createUser.success', { name: createdName }) : t('createUser.error'),
    };
    if (created) resetForm();
  };

  const resetForm = () => {
    firstName.value = '';
    lastName.value = '';
    email.value = '';
    password.value = '';
    role.value = 'USER';
    valid.value = false;
    nextTick(() => formRef.value?.resetValidation());
  };
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('createUser.label') }}</h1>
    <v-card class="pa-6">
      <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
        <UserNameFields v-model:first-name="firstName" v-model:last-name="lastName" />
        <v-text-field
          v-model="email"
          :label="t('createUser.email')"
          :rules="[rules.required, rules.email]"
          type="email"
          prepend-inner-icon="mdi-email-outline"
          class="mb-2"
          required
        />
        <PasswordField
          v-model="password"
          :label="t('createUser.password')"
          :rules="[rules.required, rules.passwordLength]"
          class="mb-4"
        />
        <UserRoleField v-model="role" />
        <div class="d-flex ga-3">
          <v-btn to="/admin/users" variant="outlined" color="primary" size="large" class="flex-grow-1">{{
            t('createUser.cancel')
          }}</v-btn>
          <v-btn
            type="submit"
            color="primary"
            size="large"
            class="flex-grow-1"
            :loading="saving"
            :disabled="!valid"
          >
            {{ t('createUser.label') }}
          </v-btn>
        </div>
      </v-form>
    </v-card>
    <v-snackbar
      v-model="feedback.show"
      :color="feedback.success ? 'primary' : 'error'"
      timeout="3000"
      location="bottom right"
    >
      {{ feedback.text }}
      <template v-if="feedback.success" #actions>
        <v-btn to="/admin/users" variant="text" color="secondary">{{ t('nav.users') }}</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 560px;
  }
</style>
