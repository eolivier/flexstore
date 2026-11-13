<script setup lang="ts">
  import { nextTick, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import FormActions from '@/components/form/FormActions.vue';
  import PasswordField from '@/components/account/PasswordField.vue';
  import UserNameFields from '@/components/account/UserNameFields.vue';
  import type { AccountStatus, User } from '@/models/user';
  import UserRoleField from './UserRoleField.vue';
  import AccountStatusField from './AccountStatusField.vue';
  import AdminTwoFactorAuthenticationField from './AdminTwoFactorAuthenticationField.vue';

  export interface UserFormValues {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    role: NonNullable<User['role']>;
    status: AccountStatus | null;
  }

  // What only an existing account has: its status, its two-factor authentication and its owner
  export interface ExistingAccount {
    id: string;
    status: AccountStatus | null;
    twoFactorEnabled: boolean;
    ownAccount: boolean;
  }

  const props = defineProps<{
    initial: Pick<UserFormValues, 'firstName' | 'lastName' | 'email' | 'role'>;
    saving: boolean;
    submitLabel?: string;
    // Absent when the user is created: then the password is asked, which an existing account does not show
    account?: ExistingAccount;
    errorMessage?: string;
  }>();
  const emit = defineEmits<{ submit: [values: UserFormValues]; twoFactorDisabled: [] }>();

  const { t } = useI18n();
  const firstName = ref(props.initial.firstName);
  const lastName = ref(props.initial.lastName);
  const email = ref(props.initial.email);
  const password = ref('');
  const role = ref(props.initial.role);
  // The status is changed by its own request, only when the administrator chose another one
  const status = ref(props.account?.status ?? null);
  const valid = ref(false);
  const formRef = ref<{ validate: () => Promise<unknown> } | null>(null);

  const rules = {
    required: (v: string) => !!v || t('createUser.mandatoryField'),
    email: (v: string) => /.+@.+\..+/.test(v) || t('createUser.invalidEmail'),
    passwordLength: (v: string) => v.length >= 8 || t('createUser.passwordTooShort'),
  };

  onMounted(async () => {
    if (!props.account) return;
    // A field that the stored user leaves empty
    // (e.g. the last name of a one-word name split by the migration) is
    // shown in error at once: otherwise the save button would stay disabled without telling why
    await nextTick();
    await formRef.value?.validate();
  });

  function submit() {
    if (!valid.value) return;
    emit('submit', {
      firstName: firstName.value,
      lastName: lastName.value,
      email: email.value,
      password: password.value,
      role: role.value,
      status: status.value,
    });
  }
</script>

<template>
  <v-form ref="formRef" v-model="valid" @submit.prevent="submit">
    <UserNameFields v-model:first-name="firstName" v-model:last-name="lastName" />
    <v-text-field
      v-model="email"
      :label="t('createUser.email')"
      :rules="[rules.required, rules.email]"
      type="email"
      prepend-inner-icon="mdi-email-outline"
      class="mb-2"
    />
    <PasswordField
      v-if="!account"
      v-model="password"
      :label="t('createUser.password')"
      :rules="[rules.required, rules.passwordLength]"
      class="mb-4"
    />
    <UserRoleField v-model="role" />
    <template v-if="account">
      <!-- An administrator cannot change the status of their own account -->
      <AccountStatusField v-if="status" v-model="status" :disabled="account.ownAccount" />
      <AdminTwoFactorAuthenticationField
        v-if="account.twoFactorEnabled"
        :user-id="account.id"
        :own-account="account.ownAccount"
        @disabled="emit('twoFactorDisabled')"
      />
      <p class="text-caption text-medium-emphasis mb-4">{{ t('editUser.passwordNote') }}</p>
    </template>
    <v-alert v-if="errorMessage" type="error" variant="tonal" density="compact" class="mb-4">
      {{ errorMessage }}
    </v-alert>
    <FormActions class="mt-2" cancel-to="/admin/users" :loading="saving" :disabled="!valid">{{
      submitLabel
    }}</FormActions>
  </v-form>
</template>
