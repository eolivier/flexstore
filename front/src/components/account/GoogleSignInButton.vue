<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { googleClientId, googleSignInIsAvailable, loadGoogleIdentity } from '@/utils/googleIdentity';
  import { logFailure } from '@/utils/logFailure';

  // The ID token that Google gave once the person chose their account
  const emit = defineEmits<{ credential: [idToken: string] }>();
  const { t, locale } = useI18n();

  const available = googleSignInIsAvailable();
  const buttonContainer = ref<HTMLElement | null>(null);
  const failed = ref(false);

  onMounted(async () => {
    if (!available || !buttonContainer.value) return;
    try {
      const google = await loadGoogleIdentity();
      google.accounts.id.initialize({
        client_id: googleClientId(),
        callback: (response) => emit('credential', response.credential),
      });
      google.accounts.id.renderButton(buttonContainer.value, {
        type: 'standard',
        theme: 'outline',
        size: 'large',
        text: 'signin_with',
        shape: 'rectangular',
        width: 320,
        locale: locale.value,
      });
    } catch (error) {
      logFailure('sign in with Google', error);
      failed.value = true;
    }
  });
</script>

<template>
  <div v-if="available">
    <div class="d-flex align-center my-4">
      <v-divider />
      <span class="text-body-2 text-medium-emphasis mx-3">{{ t('login.or') }}</span>
      <v-divider />
    </div>
    <div ref="buttonContainer" class="d-flex justify-center" data-testid="google-sign-in-button" />
    <v-alert v-if="failed" type="warning" density="compact" class="mt-2">{{ t('login.googleUnavailable') }}</v-alert>
  </div>
</template>
