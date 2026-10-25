<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';
  import ResendVerificationEmailButton from './ResendVerificationEmailButton.vue';

  const { t } = useI18n();
  const authStore = useAuthStore();

  // Only for a customer whose email is not verified yet
  const pendingVerification = computed(() => authStore.currentUser?.status === 'PENDING_VERIFICATION');
</script>

<template>
  <v-alert v-if="pendingVerification" type="info" variant="tonal" icon="mdi-email-check-outline" class="mb-6">
    <div class="font-weight-bold mb-1">{{ t('emailVerification.pendingTitle') }}</div>
    <p class="text-body-2 mb-3">
      {{ t('emailVerification.pendingText', { email: authStore.currentUser?.email }) }}
    </p>
    <ResendVerificationEmailButton />
  </v-alert>
</template>
