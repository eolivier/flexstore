<script setup lang="ts">
  import { nextTick, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';
  import TwoFactorEnrollmentDialog from '@/components/account/TwoFactorEnrollmentDialog.vue';
  import TwoFactorDisableDialog from '@/components/account/TwoFactorDisableDialog.vue';

  const { t } = useI18n();
  const authStore = useAuthStore();

  const enrollmentOpen = ref(false);
  const disableOpen = ref(false);
  const feedback = ref({ show: false, text: '' });

  // Close the previous message first, so that a new one is shown even if the previous one is still open
  async function showFeedback(text: string) {
    feedback.value.show = false;
    await nextTick();
    feedback.value = { show: true, text };
  }
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

    <TwoFactorEnrollmentDialog v-model="enrollmentOpen" @enabled="showFeedback(t('twoFactorAuth.enabled'))" />
    <TwoFactorDisableDialog v-model="disableOpen" @disabled="showFeedback(t('twoFactorAuth.disabled'))" />

    <v-snackbar v-model="feedback.show" color="primary" timeout="3000" location="bottom right">
      {{ feedback.text }}
    </v-snackbar>
  </v-card>
</template>
