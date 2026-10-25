<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute } from 'vue-router';
  import { usePasswordResetStore, type PasswordReset } from '@/stores/passwordReset.store';
  import PasswordField from './PasswordField.vue';

  const { t } = useI18n();
  const route = useRoute();
  const passwordResetStore = usePasswordResetStore();

  // The page of the link sent by email: /reset-password?token=...
  const token = typeof route.query.token === 'string' ? route.query.token : '';
  const password = ref('');
  const valid = ref(false);
  const saving = ref(false);
  // A link without token is invalid, without asking the server; null until the form is sent
  const result = ref<PasswordReset | null>(token ? null : 'unknown');

  // The link does not work: the customer asks for a new one
  const linkRefused = computed(() => result.value === 'expired' || result.value === 'unknown');

  const rules = {
    required: (v: string) => !!v || t('login.mandatoryField'),
    passwordLength: (v: string) => v.length >= 8 || t('createUser.passwordTooShort'),
  };

  async function submit() {
    if (!valid.value) return;
    saving.value = true;
    result.value = await passwordResetStore.resetPassword(token, password.value);
    saving.value = false;
  }
</script>

<template>
  <v-container class="py-12 d-flex justify-center align-center" style="min-height: 70vh">
    <v-card width="100%" max-width="420" class="pa-8">
      <template v-if="result === 'reset'">
        <v-icon icon="mdi-check-circle-outline" size="56" color="success" class="mb-4" />
        <h1 class="text-h5 font-weight-bold mb-2">{{ t('passwordReset.reset.title') }}</h1>
        <p class="text-body-2 text-medium-emphasis mb-6">{{ t('passwordReset.reset.text') }}</p>
        <v-btn to="/login" color="primary" block>{{ t('nav.login') }}</v-btn>
      </template>
      <template v-else-if="linkRefused">
        <v-icon icon="mdi-link-variant-off" size="56" color="warning" class="mb-4" />
        <h1 class="text-h5 font-weight-bold mb-2">{{ t(`passwordReset.${result}.title`) }}</h1>
        <p class="text-body-2 text-medium-emphasis mb-6">{{ t(`passwordReset.${result}.text`) }}</p>
        <v-btn to="/forgot-password" color="primary" block>{{ t('passwordReset.askNewLink') }}</v-btn>
      </template>
      <template v-else>
        <h1 class="text-h5 font-weight-bold mb-1">{{ t('passwordReset.chooseTitle') }}</h1>
        <p class="text-body-2 text-medium-emphasis mb-6">{{ t('passwordReset.chooseText') }}</p>
        <v-form v-model="valid" @submit.prevent="submit">
          <PasswordField
            v-model="password"
            :label="t('signUp.password')"
            :rules="[rules.required, rules.passwordLength]"
            class="mb-4"
          />
          <v-alert v-if="result" type="error" density="compact" class="mb-4">
            {{ t(`passwordReset.${result}Error`) }}
          </v-alert>
          <v-btn type="submit" color="primary" size="large" block :loading="saving" :disabled="!valid">
            {{ t('passwordReset.save') }}
          </v-btn>
        </v-form>
      </template>
    </v-card>
  </v-container>
</template>
