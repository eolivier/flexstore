<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import { useAuthStore } from '@/stores/auth.store';
  import { useEmailVerificationStore, type EmailVerification } from '@/stores/emailVerification.store';
  import ResendVerificationEmailButton from './ResendVerificationEmailButton.vue';

  const { t } = useI18n();
  const route = useRoute();
  const authStore = useAuthStore();
  const emailVerificationStore = useEmailVerificationStore();

  // null while the link is checked
  const verification = ref<EmailVerification | null>(null);

  const icons: Record<EmailVerification, string> = {
    verified: 'mdi-check-circle-outline',
    expired: 'mdi-timer-sand-complete',
    unknown: 'mdi-link-variant-off',
    error: 'mdi-alert-circle-outline',
  };

  // The page of the link sent by email: /verify-email?token=...
  onMounted(async () => {
    const token = route.query.token;
    verification.value = typeof token === 'string' && token ? await emailVerificationStore.verifyEmail(token) : 'unknown';
  });
</script>

<template>
  <v-container class="page py-12">
    <v-card class="pa-8 text-center">
      <v-progress-circular v-if="verification === null" indeterminate color="primary" class="my-6" />
      <template v-else>
        <v-icon :icon="icons[verification]" size="56" :color="verification === 'verified' ? 'success' : 'warning'" class="mb-4" />
        <h1 class="text-h5 font-weight-bold mb-2">{{ t(`emailVerification.${verification}.title`) }}</h1>
        <p class="text-body-2 text-medium-emphasis mb-6">{{ t(`emailVerification.${verification}.text`) }}</p>
        <v-btn v-if="verification === 'verified'" to="/products/" color="primary">{{ t('emailVerification.continueShopping') }}</v-btn>
        <ResendVerificationEmailButton v-else-if="authStore.isAuthenticated" />
        <v-btn v-else :to="{ path: '/login', query: { redirect: '/user-profile' } }" color="primary">
          {{ t('emailVerification.signInForNewLink') }}
        </v-btn>
      </template>
    </v-card>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 560px;
  }
</style>
