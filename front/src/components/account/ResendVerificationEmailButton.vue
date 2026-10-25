<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useEmailVerificationStore } from '@/stores/emailVerification.store';

  const { t } = useI18n();
  const emailVerificationStore = useEmailVerificationStore();

  const sending = ref(false);
  // null before the first click
  const sent = ref<boolean | null>(null);

  async function resend() {
    sending.value = true;
    sent.value = await emailVerificationStore.sendVerificationEmail();
    sending.value = false;
  }
</script>

<template>
  <div>
    <v-btn
      variant="tonal"
      color="primary"
      prepend-icon="mdi-email-fast-outline"
      :loading="sending"
      :disabled="sent === true"
      @click="resend"
    >
      {{ t('emailVerification.resend') }}
    </v-btn>
    <p v-if="sent === true" class="text-body-2 mt-2">{{ t('emailVerification.sent') }}</p>
    <p v-else-if="sent === false" class="text-body-2 text-error mt-2">{{ t('emailVerification.sendError') }}</p>
  </div>
</template>
