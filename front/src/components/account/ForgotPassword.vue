<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import { usePasswordResetStore } from '@/stores/passwordReset.store';

  const { t } = useI18n();
  const route = useRoute();
  const passwordResetStore = usePasswordResetStore();

  // Filled in with the email typed on the login page
  const email = ref(typeof route.query.email === 'string' ? route.query.email : '');
  const valid = ref(false);
  const sending = ref(false);
  // null until the form is sent
  const received = ref<boolean | null>(null);

  const rules = {
    required: (v: string) => !!v?.trim() || t('login.mandatoryField'),
    email: (v: string) => /.+@.+\..+/.test(v) || t('login.invalidEmail'),
  };

  async function submit() {
    if (!valid.value) return;
    sending.value = true;
    received.value = await passwordResetStore.requestReset(email.value);
    sending.value = false;
  }
</script>

<template>
  <v-container class="py-12 d-flex justify-center align-center" style="min-height: 70vh">
    <v-card width="100%" max-width="420" class="pa-8">
      <h1 class="text-h5 font-weight-bold mb-1">{{ t('passwordReset.forgotTitle') }}</h1>
      <template v-if="received">
        <v-alert type="success" variant="tonal" class="my-4">{{ t('passwordReset.sent', { email }) }}</v-alert>
        <v-btn to="/login" color="primary" block>{{ t('passwordReset.backToLogin') }}</v-btn>
      </template>
      <template v-else>
        <p class="text-body-2 text-medium-emphasis mb-6">{{ t('passwordReset.forgotText') }}</p>
        <v-form v-model="valid" @submit.prevent="submit">
          <v-text-field
            v-model="email"
            :label="t('login.email')"
            :rules="[rules.required, rules.email]"
            type="email"
            prepend-inner-icon="mdi-email-outline"
            autocomplete="email"
            required
            class="mb-4"
          />
          <v-alert v-if="received === false" type="error" density="compact" class="mb-4">
            {{ t('passwordReset.sendError') }}
          </v-alert>
          <v-btn type="submit" color="primary" size="large" block :loading="sending" :disabled="!valid">
            {{ t('passwordReset.send') }}
          </v-btn>
        </v-form>
      </template>
    </v-card>
  </v-container>
</template>
