<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useAuthStore } from '@/stores/auth.store';
  import { useI18n } from 'vue-i18n';
  import TwoFactorChallengeForm from '@/components/account/TwoFactorChallengeForm.vue';

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

  const rules = {
    required: (v: string) => !!v || t('login.mandatoryField'),
    email: (v: string) => /.+@.+\..+/.test(v) || t('login.invalidEmail'),
  };

  function redirectAfterSignIn() {
    const redirectTo = (route.query.redirect as string) || (authStore.isAdmin ? '/admin' : '/user-profile');
    router.push(redirectTo);
  }

  const submitForm = async () => {
    if (!valid.value) return;

    loading.value = true;
    const outcome = await authStore.login(email.value, password.value);
    loading.value = false;

    if (outcome === 'signedIn') {
      redirectAfterSignIn();
    } else if (outcome === 'twoFactorRequired') {
      twoFactorStep.value = true;
    }
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

          <v-text-field
            v-model="password"
            :label="t('login.password')"
            :rules="[rules.required]"
            type="password"
            prepend-inner-icon="mdi-lock-outline"
            autocomplete="current-password"
            required
            class="mb-1"
          />
          <div class="text-right mb-4">
            <router-link :to="{ path: '/forgot-password', query: email ? { email } : {} }" class="text-body-2">
              {{ t('login.forgotPassword') }}
            </router-link>
          </div>

          <v-alert v-if="authStore.loginError" type="error" density="compact" class="mb-4">
            {{ authStore.refusedStatus ? t(`login.refused.${authStore.refusedStatus}`) : t('login.error') }}
          </v-alert>

          <v-btn type="submit" color="primary" size="large" block :loading="loading" :disabled="!valid">
            {{ t('login.submit') }}
          </v-btn>
        </v-form>
        <p class="text-body-2 text-center mt-6">
          {{ t('login.noAccount') }}
          <router-link :to="{ path: '/sign-up', query: route.query }">{{ t('nav.signUp') }}</router-link>
        </p>
      </template>
    </v-card>
  </v-container>
</template>
