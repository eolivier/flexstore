<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useAuthStore } from '@/stores/auth.store';
  import { useI18n } from 'vue-i18n';

  const email = ref('');
  const password = ref('');
  const valid = ref(false);
  const loading = ref(false);
  const formRef = ref(null);

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
      const redirectTo = (route.query.redirect as string) || '/admin';
      router.push(redirectTo);
    }
  };
</script>

<template>
  <v-container class="pa-4 d-flex justify-center align-center" style="min-height: 80vh;">
    <v-card max-width="400" class="pa-6" elevation="4">
      <v-card-title class="text-h5 font-weight-bold mb-4">
        {{ t('login.title') }}
      </v-card-title>

      <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
        <v-text-field
          v-model="email"
          :label="t('login.email')"
          :rules="[rules.required, rules.email]"
          type="email"
          prepend-inner-icon="mdi-email"
          required
          class="mb-2"
        />

        <v-text-field
          v-model="password"
          :label="t('login.password')"
          :rules="[rules.required]"
          type="password"
          prepend-inner-icon="mdi-lock"
          required
          class="mb-4"
        />

        <v-alert
          v-if="authStore.loginError"
          type="error"
          density="compact"
          class="mb-4"
        >
          {{ t('login.error') }}
        </v-alert>

        <v-btn
          type="submit"
          color="primary"
          block
          :loading="loading"
          :disabled="!valid"
        >
          {{ t('login.submit') }}
        </v-btn>
      </v-form>
    </v-card>
  </v-container>
</template>
