<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';
  import { useSnackbar } from '@/composables/useSnackbar';
  import TwoFactorEnrollmentDialog from '@/components/account/TwoFactorEnrollmentDialog.vue';
  import TwoFactorDisableDialog from '@/components/account/TwoFactorDisableDialog.vue';

  const { t } = useI18n();
  const authStore = useAuthStore();
  const snackbar = useSnackbar();

  const enrollmentOpen = ref(false);
  const disableOpen = ref(false);
</script>

<template>
  <v-card class="pa-6">
    <h2 class="text-h6 font-weight-bold mb-1">{{ t('twoFactorAuth.title') }}</h2>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t('twoFactorAuth.text') }}</p>

    <template v-if="authStore.currentUser?.twoFactorEnabled">
      <v-alert type="success" variant="tonal" density="compact" class="mb-4">
        {{ t('twoFactorAuth.enabledText') }}
      </v-alert>
      <v-btn color="error" variant="outlined" @click="disableOpen = true">{{ t('twoFactorAuth.disable') }}</v-btn>
    </template>
    <v-btn v-else color="primary" @click="enrollmentOpen = true">{{ t('twoFactorAuth.enable') }}</v-btn>

    <TwoFactorEnrollmentDialog v-model="enrollmentOpen" @enabled="snackbar.success(t('twoFactorAuth.enabled'))" />
    <TwoFactorDisableDialog v-model="disableOpen" @disabled="snackbar.success(t('twoFactorAuth.disabled'))" />
  </v-card>
</template>
