<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSnackbar } from '@/composables/useSnackbar';
  import { useUsersStore, type EditedUser, type UserEdition } from '@/stores/users.store';
  import { useAuthStore } from '@/stores/auth.store';
  import type { AccountStatus } from '@/models/user';
  import UserForm, { type ExistingAccount, type UserFormValues } from './UserForm.vue';

  const props = defineProps<{ id: string }>();

  const { t } = useI18n();
  const snackbar = useSnackbar();
  const usersStore = useUsersStore();
  const authStore = useAuthStore();

  // null while loading, false when the user cannot be read
  const editedUser = ref<EditedUser | null | false>(null);
  const saving = ref(false);
  const edition = ref<UserEdition | null>(null);
  const storedStatus = ref<AccountStatus | null>(null);
  // The two-factor authentication is disabled by its own request, from its own field
  const twoFactorEnabled = ref(false);

  const account = computed<ExistingAccount>(() => ({
    id: props.id,
    status: storedStatus.value,
    twoFactorEnabled: twoFactorEnabled.value,
    // An administrator cannot change the status of their own account
    ownAccount: authStore.currentUser?.id === props.id,
  }));
  const errorMessage = computed(() =>
    edition.value === 'emailUsed' || edition.value === 'error' ? t(`editUser.${edition.value}`) : undefined
  );

  onMounted(async () => {
    const user = await usersStore.fetchUser(props.id);
    editedUser.value = user
      ? { firstName: user.firstName, lastName: user.lastName, email: user.email, role: user.role ?? 'USER' }
      : false;
    storedStatus.value = user?.status ?? null;
    twoFactorEnabled.value = user?.twoFactorEnabled ?? false;
  });

  function onTwoFactorDisabled() {
    twoFactorEnabled.value = false;
    snackbar.success(t('editUser.twoFactorDisabled'));
  }

  async function save({ firstName, lastName, email, role, status }: UserFormValues) {
    saving.value = true;
    edition.value = await usersStore.updateUser(props.id, { firstName, lastName, email, role });
    if (edition.value === 'saved' && status && status !== storedStatus.value) {
      const changed = await usersStore.changeStatus(props.id, status);
      if (changed) storedStatus.value = status;
      else edition.value = 'error';
    }
    saving.value = false;
    if (edition.value === 'saved') {
      snackbar.success(t('editUser.saved'), { action: { text: t('nav.users'), to: '/admin/users' } });
    }
  }
</script>

<template>
  <v-container class="page py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('editUser.title') }}</h1>
    <v-skeleton-loader v-if="editedUser === null" type="article" class="rounded-xl" />
    <v-alert v-else-if="editedUser === false" type="error" variant="tonal">{{
      t('editUser.loadError')
    }}</v-alert>
    <v-card v-else class="pa-6">
      <UserForm
        :initial="editedUser"
        :account="account"
        :saving="saving"
        :error-message="errorMessage"
        @submit="save"
        @two-factor-disabled="onTwoFactorDisabled"
      />
    </v-card>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 560px;
  }
</style>
