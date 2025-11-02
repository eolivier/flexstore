<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useUsersStore } from '@/stores/users.store.ts';
  import ConfirmDialog from '@/components/ConfirmDialog.vue';
  import { useI18n } from 'vue-i18n';

  const usersStore = useUsersStore();
  const dialog = ref(false);
  const userIdToDelete = ref<number|null>(null);
  const { t } = useI18n();

  onMounted(() => {
    usersStore.fetchUsers();
  });

  function openDeleteDialog(id: number) {
    userIdToDelete.value = id;
    dialog.value = true;
  }

  function confirmDelete() {
    if (userIdToDelete.value !== null) {
      usersStore.deleteUser(userIdToDelete.value);
      userIdToDelete.value = null;
    }
  }

  const headers = [
    { title: 'ID', value: 'id' },
    { title: 'Nom', value: 'name' },
    { title: 'Email', value: 'email' },
    { title: 'Actions', value: 'actions', sortable: false },
  ];
</script>

<template>
  <v-container>
    <h1 class="text-h5 font-weight-bold mb-6">Users</h1>
    <v-data-table :headers="headers" :items="usersStore.users" class="elevation-1">
      <template #item.actions="{ item }">
        <v-btn color="error" @click="openDeleteDialog(item.id)"> Delete </v-btn>
      </template>
    </v-data-table>
    <ConfirmDialog
      v-model="dialog"
      :title="t('confirmation')"
      :message="t('confirmUserDeletion')"
      @confirm="confirmDelete"
    />
  </v-container>
</template>
