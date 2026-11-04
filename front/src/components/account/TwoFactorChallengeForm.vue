<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';

  const emit = defineEmits<{ verified: []; cancel: [] }>();
  const { t } = useI18n();
  const authStore = useAuthStore();

  const code = ref('');
  const loading = ref(false);
  const error = ref('');

  async function submit() {
    loading.value = true;
    const outcome = await authStore.verifyTwoFactorCode(code.value);
    loading.value = false;
    if (outcome === 'signedIn') {
      emit('verified');
    } else {
      error.value = outcome === 'invalidCode' ? t('login.twoFactor.invalidCode') : t('login.twoFactor.error');
    }
  }

  function cancel() {
    authStore.cancelTwoFactorChallenge();
    emit('cancel');
  }
</script>

<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-1">{{ t('login.twoFactor.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">{{ t('login.twoFactor.subtitle') }}</p>
    <v-form @submit.prevent="submit">
      <v-text-field
        v-model="code"
        :label="t('login.twoFactor.codeLabel')"
        inputmode="numeric"
        maxlength="6"
        autofocus
        class="mb-1"
      />
      <v-alert v-if="error" type="error" density="compact" class="mb-4">{{ error }}</v-alert>
      <v-btn type="submit" color="primary" size="large" block :loading="loading" :disabled="code.length !== 6">
        {{ t('login.twoFactor.submit') }}
      </v-btn>
      <v-btn variant="text" block class="mt-2" @click="cancel">{{ t('login.twoFactor.back') }}</v-btn>
    </v-form>
  </div>
</template>
