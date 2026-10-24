<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useUsersStore, type EditedUser, type UserEdition } from '@/stores/users.store';
  import UserRoleField from './UserRoleField.vue';
  import UserNameFields from '@/components/account/UserNameFields.vue';

  const props = defineProps<{ id: string }>();

  const { t } = useI18n();
  const usersStore = useUsersStore();

  // null while loading, false when the user cannot be read
  const editedUser = ref<EditedUser | null | false>(null);
  const valid = ref(false);
  const saving = ref(false);
  const edition = ref<UserEdition | null>(null);

  const rules = {
    required: (v: string) => !!v || t('createUser.mandatoryField'),
    email: (v: string) => /.+@.+\..+/.test(v) || t('createUser.invalidEmail'),
  };

  onMounted(async () => {
    const user = await usersStore.fetchUser(props.id);
    editedUser.value = user ? { firstName: user.firstName, lastName: user.lastName, email: user.email, role: user.role ?? 'USER' } : false;
  });

  async function save() {
    if (!valid.value || !editedUser.value) return;
    saving.value = true;
    edition.value = await usersStore.updateUser(props.id, editedUser.value);
    saving.value = false;
  }
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('editUser.title') }}</h1>
    <v-skeleton-loader v-if="editedUser === null" type="article" class="rounded-xl" />
    <v-alert v-else-if="editedUser === false" type="error" variant="tonal">{{ t('editUser.loadError') }}</v-alert>
    <v-card v-else class="pa-6">
      <v-form v-model="valid" @submit.prevent="save">
        <UserNameFields v-model:first-name="editedUser.firstName" v-model:last-name="editedUser.lastName" />
        <v-text-field v-model="editedUser.email" :label="t('createUser.email')" :rules="[rules.required, rules.email]" type="email" prepend-inner-icon="mdi-email-outline" class="mb-2" />
        <UserRoleField v-model="editedUser.role" />
        <p class="text-caption text-medium-emphasis mb-4">{{ t('editUser.passwordNote') }}</p>
        <v-alert v-if="edition === 'emailUsed' || edition === 'error'" type="error" variant="tonal" density="compact" class="mb-4">
          {{ t(`editUser.${edition}`) }}
        </v-alert>
        <div class="d-flex ga-3">
          <v-btn to="/admin/users" variant="outlined" color="primary" size="large" class="flex-grow-1">{{ t('editUser.cancel') }}</v-btn>
          <v-btn type="submit" color="primary" size="large" class="flex-grow-1" :loading="saving" :disabled="!valid">{{ t('editUser.save') }}</v-btn>
        </div>
      </v-form>
    </v-card>
    <v-snackbar :model-value="edition === 'saved'" color="primary" timeout="3000" location="bottom right" @update:model-value="edition = null">
      {{ t('editUser.saved') }}
      <template #actions>
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
