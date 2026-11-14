<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { useUsersStore } from '@/stores/users.store.ts';
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSnackbar } from '@/composables/useSnackbar';
  import { FullName } from '@/models/user';
  import UserForm, { type UserFormValues } from './UserForm.vue';

  // A new user is a simple user unless the administrator chooses otherwise
  const emptyUser = { firstName: '', lastName: '', email: '', role: 'USER' } as const;
  const saving = ref(false);
  // Changing the key gives an empty form again
  const formKey = ref(0);

  const usersStore = useUsersStore();
  const { t } = useI18n();
  const snackbar = useSnackbar();

  const create = async ({ firstName, lastName, email, password, role }: UserFormValues) => {
    saving.value = true;
    const createdName = FullName.of({ firstName, lastName }).value;
    const created = await usersStore.addUser({ firstName, lastName, email, password, role });
    saving.value = false;
    if (created) {
      snackbar.success(t('createUser.success', { name: createdName }), {
        action: { text: t('nav.users'), to: '/admin/users' },
      });
      formKey.value++;
    } else snackbar.error(t('createUser.error'));
  };
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('createUser.label') }}</h1>
    <v-card class="pa-6">
      <UserForm
        :key="formKey"
        :initial="emptyUser"
        :submit-label="t('createUser.label')"
        :saving="saving"
        @submit="create"
      />
    </v-card>
  </v-container>
</template>
