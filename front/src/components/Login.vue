<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useAuthStore } from '@/stores/auth.store';
  import { useI18n } from 'vue-i18n';

  const email = ref('');
  const password = ref('');
  const valid = ref(false);
  const loading = ref(false);

  const authStore = useAuthStore();
  const router = useRouter();
  const route = useRoute();
  const { t } = useI18n();

  const rules = {
    required: (v: string) => !!v || t('login.mandatoryField'),
    email: (v: string) => /.+@.+\..+/.test(v) || t('login.invalidEmail'),
  };

  const submitForm = async () => {
    if (!valid.value) return;

    loading.value = true;
    const success = await authStore.login(email.value, password.value);
    loading.value = false;

    if (success) {
      const redirectTo = (route.query.redirect as string) || (authStore.isAdmin ? '/admin' : '/user-profile');
      router.push(redirectTo);
    }
  };
</script>

<template>
  <v-container class="py-12 d-flex justify-center align-center" style="min-height: 70vh;">
    <v-card width="100%" max-width="420" class="pa-8">
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
          class="mb-4"
        />

        <v-alert
          v-if="authStore.loginError"
          type="error"
          density="compact"
          class="mb-4"
        >
          {{ authStore.refusedStatus ? t(`login.refused.${authStore.refusedStatus}`) : t('login.error') }}
        </v-alert>

        <v-btn
          type="submit"
          color="primary"
          size="large"
          block
          :loading="loading"
          :disabled="!valid"
        >
          {{ t('login.submit') }}
        </v-btn>
      </v-form>
      <p class="text-body-2 text-center mt-6">
        {{ t('login.noAccount') }}
        <router-link :to="{ path: '/sign-up', query: route.query }">{{ t('nav.signUp') }}</router-link>
      </p>
    </v-card>
  </v-container>
</template>
