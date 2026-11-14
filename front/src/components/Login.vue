<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useAuthStore, type LoginOutcome } from '@/stores/auth.store';
  import { useI18n } from 'vue-i18n';
  import { useFieldRules } from '@/composables/useFieldRules';
  import TwoFactorChallengeForm from '@/components/account/TwoFactorChallengeForm.vue';
  import PasswordField from '@/components/account/PasswordField.vue';
  import GoogleSignInButton from '@/components/account/GoogleSignInButton.vue';
  import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';

  const email = ref('');
  const password = ref('');
  const valid = ref(false);
  const loading = ref(false);
  // A local step, set once the password is right and a second factor is asked: it does not depend on when the
  // store clears its own pending challenge, so the "verified" event it carries is never missed
  const twoFactorStep = ref(false);

  const authStore = useAuthStore();
  const router = useRouter();
  const route = useRoute();
  const { t } = useI18n();

  const rules = useFieldRules();

  function redirectAfterSignIn() {
    const redirectTo = (route.query.redirect as string) || (authStore.isAdmin ? '/admin' : '/user-profile');
    router.push(redirectTo);
  }

  function continueAfter(outcome: LoginOutcome) {
    if (outcome === 'signedIn') {
      redirectAfterSignIn();
    } else if (outcome === 'twoFactorRequired') {
      twoFactorStep.value = true;
    }
  }

  const submitForm = async () => {
    if (!valid.value) return;

    loading.value = true;
    const outcome = await authStore.login(email.value, password.value);
    loading.value = false;
    continueAfter(outcome);
  };

  const signInWithGoogle = async (idToken: string) => {
    loading.value = true;
    const outcome = await authStore.loginWithGoogle(idToken);
    loading.value = false;
    continueAfter(outcome);
  };

  function backToCredentials() {
    twoFactorStep.value = false;
  }
</script>

<template>
  <v-container class="py-12 d-flex justify-center align-center" style="min-height: 70vh">
    <v-card width="100%" max-width="420" class="pa-8">
      <TwoFactorChallengeForm v-if="twoFactorStep" @verified="redirectAfterSignIn" @cancel="backToCredentials" />
      <template v-else>
        <h1 class="text-h5 font-weight-bold mb-1">{{ t('login.title') }}</h1>
        <p class="text-body-2 text-medium-emphasis mb-6">{{ t('login.subtitle') }}</p>

        <v-form v-model="valid" @submit.prevent="submitForm">
          <v-text-field
            v-model="email"
            :label="t('login.email')"
            :rules="[rules.required, rules.email]"
            type="email"
            prepend-inner-icon="mdi-email-outline"
            autocomplete="email"
            required
            class="mb-2"
          />

          <PasswordField
            v-model="password"
            :label="t('login.password')"
            :rules="[rules.required]"
            autocomplete="current-password"
            class="mb-1"
          />
          <div class="text-right mb-4">
            <router-link :to="{ path: '/forgot-password', query: email ? { email } : {} }" class="text-body-2">
              {{ t('login.forgotPassword') }}
            </router-link>
          </div>

          <FeedbackAlert v-if="authStore.loginError" type="error" compact class="mb-4">
            {{ authStore.refusedStatus ? t(`login.refused.${authStore.refusedStatus}`) : t('login.error') }}
          </FeedbackAlert>

          <v-btn type="submit" color="primary" size="large" block :loading="loading" :disabled="!valid">
            {{ t('login.submit') }}
          </v-btn>
        </v-form>
        <GoogleSignInButton @credential="signInWithGoogle" />
        <p class="text-body-2 text-center mt-6">
          {{ t('login.noAccount') }}
          <router-link :to="{ path: '/sign-up', query: route.query }">{{ t('nav.signUp') }}</router-link>
        </p>
      </template>
    </v-card>
  </v-container>
</template>
