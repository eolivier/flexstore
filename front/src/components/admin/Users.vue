<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useUsersStore } from '@/stores/users.store.ts';
  import ConfirmDialog from '@/components/ConfirmDialog.vue';
  import { FullName, type AccountStatus, type User } from '@/models/user';
  import { useI18n } from 'vue-i18n';

  const usersStore = useUsersStore();
  const dialog = ref(false);
  const userIdToDelete = ref<string | null>(null);
  const feedback = ref({ show: false, success: true, text: '' });
  const { t } = useI18n();

  onMounted(() => {
    usersStore.fetchUsers();
  });

  function openDeleteDialog(id: string) {
    userIdToDelete.value = id;
    dialog.value = true;
  }

  async function confirmDelete() {
    if (userIdToDelete.value !== null) {
      const deleted = await usersStore.deleteUser(userIdToDelete.value);
      userIdToDelete.value = null;
      feedback.value = {
        show: true,
        success: deleted,
        text: t(deleted ? 'users.deleted' : 'users.deleteError'),
      };
    }
  }

  // The statuses that prevent signing in and ordering stand out
  const statusColors: Record<AccountStatus, string | undefined> = {
    PENDING_VERIFICATION: 'info',
    ACTIVE: 'success',
    SUSPENDED: 'warning',
    LOCKED: 'error',
    DISABLED: 'error',
  };

  const headers = computed(() => [
    { title: t('table.name'), key: 'name', value: (user: User) => FullName.of(user).value },
    { title: t('table.email'), key: 'email' },
    { title: t('table.role'), key: 'role' },
    { title: t('table.status'), key: 'status' },
    { title: '', key: 'actions', sortable: false, align: 'end' as const },
  ]);
</script>

<template>
  <v-container class="py-10">
    <div class="d-flex flex-wrap align-end ga-4 mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold">{{ t('users.title') }}</h1>
        <p class="text-body-2 text-medium-emphasis mt-1">{{ t('users.count', usersStore.users.length) }}</p>
      </div>
      <v-spacer />
      <v-btn to="/admin/create-user" color="primary" prepend-icon="mdi-account-plus-outline">{{
        t('nav.createUser')
      }}</v-btn>
    </div>

    <v-data-table
      :headers="headers"
      :items="usersStore.users"
      :loading="usersStore.loading"
      :loading-text="t('users.loading')"
      item-value="id"
    >
      <template #item.name="{ item }">
        <div class="d-flex align-center py-2">
          <v-avatar color="surface-variant" size="36" class="mr-3 text-caption font-weight-bold">{{
            FullName.of(item).initials
          }}</v-avatar>
          <span class="font-weight-medium">{{ FullName.of(item).value }}</span>
        </div>
      </template>
      <template #item.role="{ item }">
        <v-chip :color="item.role === 'ADMIN' ? 'secondary' : undefined" variant="tonal" size="small">
          {{ t(`createUser.roles.${item.role ?? 'USER'}.label`) }}
        </v-chip>
      </template>
      <template #item.status="{ item }">
        <v-chip :color="statusColors[item.status ?? 'ACTIVE']" variant="tonal" size="small">
          {{ t(`accountStatus.${item.status ?? 'ACTIVE'}`) }}
        </v-chip>
      </template>
      <template #item.actions="{ item }">
        <v-btn
          :to="`/admin/users/${item.id}/edit`"
          icon="mdi-pencil-outline"
          variant="text"
          size="small"
          :aria-label="t('users.edit', { name: FullName.of(item).value })"
        />
        <v-btn
          icon="mdi-trash-can-outline"
          variant="text"
          color="error"
          size="small"
          :aria-label="t('users.delete')"
          @click="openDeleteDialog(item.id)"
        />
      </template>
    </v-data-table>

    <ConfirmDialog
      v-model="dialog"
      :title="t('confirmation')"
      :message="t('confirmUserDeletion')"
      @confirm="confirmDelete"
    />
    <v-snackbar
      v-model="feedback.show"
      :color="feedback.success ? 'primary' : 'error'"
      timeout="3000"
      location="bottom right"
    >
      {{ feedback.text }}
    </v-snackbar>
  </v-container>
</template>
