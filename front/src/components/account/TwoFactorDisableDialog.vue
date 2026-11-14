<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';
  import FormDialog from '@/components/layout/FormDialog.vue';

  const open = defineModel<boolean>({ required: true });
  const emit = defineEmits<{ disabled: [] }>();
  const { t } = useI18n();
  const authStore = useAuthStore();

  const currentPassword = ref('');
  const loading = ref(false);
  const error = ref('');

  async function confirm() {
    loading.value = true;
    const outcome = await authStore.disableTwoFactorAuthentication(currentPassword.value);
    loading.value = false;
    if (outcome === 'disabled') {
      open.value = false;
      currentPassword.value = '';
      emit('disabled');
    } else {
      error.value = outcome === 'wrongPassword' ? t('twoFactorAuth.wrongPassword') : t('twoFactorAuth.disableError');
    }
  }
</script>

<template>
  <FormDialog
    v-model="open"
    :title="t('twoFactorAuth.disableTitle')"
    :text="t('twoFactorAuth.disableText')"
    @submit="confirm"
  >
    <v-text-field
      v-model="currentPassword"
      :label="t('twoFactorAuth.currentPassword')"
      type="password"
      autocomplete="current-password"
      class="mb-1"
    />
    <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-4">{{ error }}</v-alert>
    <template #actions>
      <v-btn type="submit" color="error" :loading="loading" :disabled="!currentPassword">
        {{ t('twoFactorAuth.disable') }}
      </v-btn>
    </template>
  </FormDialog>
</template>
