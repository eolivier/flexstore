<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useUsersStore } from '@/stores/users.store';
  import FormDialog from '@/components/layout/FormDialog.vue';

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
  <FormDialog
    v-model="open"
    :title="t('editUser.twoFactorDisableTitle')"
    :text="t('editUser.twoFactorDisableText')"
    @submit="confirm"
  >
    <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-4">{{
      t('editUser.twoFactorDisableError')
    }}</v-alert>
    <template #actions>
      <v-btn type="submit" color="error" :loading="loading">{{ t('editUser.twoFactorDisableConfirm') }}</v-btn>
    </template>
  </FormDialog>
</template>
