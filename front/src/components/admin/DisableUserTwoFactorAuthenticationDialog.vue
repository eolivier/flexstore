<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useUsersStore } from '@/stores/users.store';

  const props = defineProps<{ userId: string }>();
  const open = defineModel<boolean>({ required: true });
  const emit = defineEmits<{ disabled: [] }>();
  const { t } = useI18n();
  const usersStore = useUsersStore();

  const loading = ref(false);
  const error = ref(false);

  async function confirm() {
    loading.value = true;
    const disabled = await usersStore.disableTwoFactor(props.userId);
    loading.value = false;
    if (disabled) {
      open.value = false;
      emit('disabled');
    } else {
      error.value = true;
    }
  }
</script>

<template>
  <v-dialog v-model="open" max-width="440">
    <v-card class="pa-6">
      <h2 class="text-h6 font-weight-bold mb-1">{{ t('editUser.twoFactorDisableTitle') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-4">{{ t('editUser.twoFactorDisableText') }}</p>
      <v-alert v-if="error" type="error" density="compact" class="mb-4">{{
        t('editUser.twoFactorDisableError')
      }}</v-alert>
      <div class="d-flex justify-end ga-2">
        <v-btn variant="text" @click="open = false">{{ t('editUser.cancel') }}</v-btn>
        <v-btn color="error" :loading="loading" @click="confirm">{{ t('editUser.twoFactorDisableConfirm') }}</v-btn>
      </div>
    </v-card>
  </v-dialog>
</template>
