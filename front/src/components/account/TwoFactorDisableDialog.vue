<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';

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
  <v-dialog v-model="open" max-width="440">
    <v-card class="pa-6">
      <h2 class="text-h6 font-weight-bold mb-1">{{ t('twoFactorAuth.disableTitle') }}</h2>
      <p class="text-body-2 text-medium-emphasis mb-4">{{ t('twoFactorAuth.disableText') }}</p>
      <v-form @submit.prevent="confirm">
        <v-text-field
          v-model="currentPassword"
          :label="t('twoFactorAuth.currentPassword')"
          type="password"
          autocomplete="current-password"
          class="mb-1"
        />
        <v-alert v-if="error" type="error" density="compact" class="mb-4">{{ error }}</v-alert>
        <div class="d-flex justify-end ga-2">
          <v-btn variant="text" @click="open = false">{{ t('twoFactorAuth.cancel') }}</v-btn>
          <v-btn type="submit" color="error" :loading="loading" :disabled="!currentPassword">
            {{ t('twoFactorAuth.disable') }}
          </v-btn>
        </div>
      </v-form>
    </v-card>
  </v-dialog>
</template>
