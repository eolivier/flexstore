<script setup lang="ts">
  import { nextTick, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAuthStore } from '@/stores/auth.store';

  const { t } = useI18n();
  const authStore = useAuthStore();

  const currentPassword = ref('');
  const newPassword = ref('');
  const confirmPassword = ref('');
  const showPasswords = ref(false);
  const valid = ref(false);
  const saving = ref(false);
  const formRef = ref<{ reset: () => void; resetValidation: () => void } | null>(null);
  const newPasswordField = ref<{ validate: () => void } | null>(null);
  const confirmPasswordField = ref<{ validate: () => void } | null>(null);
  const feedback = ref({ show: false, success: true, text: '' });

  const rules = {
    required: (v: string) => !!v || t('createUser.mandatoryField'),
    length: (v: string) => v.length >= 8 || t('createUser.passwordTooShort'),
    different: (v: string) => v !== currentPassword.value || t('profile.passwordUnchanged'),
    same: (v: string) => v === newPassword.value || t('profile.passwordsDiffer'),
  };

  // The rules of a field depend on the other fields: check them again when those change
  watch(currentPassword, () => newPassword.value && newPasswordField.value?.validate());
  watch(newPassword, () => confirmPassword.value && confirmPasswordField.value?.validate());

  async function showFeedback(success: boolean, text: string) {
    // Close the previous message first, so that a new one is shown even if the previous one is still open
    feedback.value.show = false;
    await nextTick();
    feedback.value = { show: true, success, text };
  }

  async function submit() {
    if (!valid.value) return;
    saving.value = true;
    const passwordChange = await authStore.changePassword(currentPassword.value, newPassword.value);
    saving.value = false;
    const messages = {
      changed: 'profile.passwordChanged',
      wrongPassword: 'profile.wrongPassword',
      error: 'profile.passwordError',
    };
    if (passwordChange === 'changed') {
      formRef.value?.reset();
      await nextTick();
      formRef.value?.resetValidation();
      showPasswords.value = false;
    }
    await showFeedback(passwordChange === 'changed', t(messages[passwordChange]));
  }
</script>

<template>
  <v-card class="pa-6">
    <h2 class="text-h6 font-weight-bold mb-1">{{ t('profile.passwordTitle') }}</h2>
    <p class="text-body-2 text-medium-emphasis mb-5">{{ t('profile.passwordText') }}</p>
    <v-form ref="formRef" v-model="valid" @submit.prevent="submit">
      <v-text-field
        v-model="currentPassword"
        :label="t('profile.currentPassword')"
        :type="showPasswords ? 'text' : 'password'"
        :rules="[rules.required]"
        prepend-inner-icon="mdi-lock-outline"
        autocomplete="current-password"
        class="mb-2"
      />
      <v-row>
        <v-col cols="12" sm="6">
          <v-text-field
            ref="newPasswordField"
            v-model="newPassword"
            :label="t('profile.newPassword')"
            :type="showPasswords ? 'text' : 'password'"
            :rules="[rules.required, rules.length, rules.different]"
            prepend-inner-icon="mdi-lock-plus-outline"
            autocomplete="new-password"
          />
        </v-col>
        <v-col cols="12" sm="6">
          <v-text-field
            ref="confirmPasswordField"
            v-model="confirmPassword"
            :label="t('profile.confirmPassword')"
            :type="showPasswords ? 'text' : 'password'"
            :rules="[rules.required, rules.same]"
            prepend-inner-icon="mdi-lock-check-outline"
            autocomplete="new-password"
          />
        </v-col>
      </v-row>
      <div class="d-flex flex-wrap align-center ga-4 mt-2">
        <v-checkbox v-model="showPasswords" :label="t('createUser.showPassword')" density="compact" hide-details />
        <v-spacer />
        <v-btn type="submit" color="primary" :loading="saving" :disabled="!valid" prepend-icon="mdi-check">
          {{ t('profile.changePassword') }}
        </v-btn>
      </div>
    </v-form>
    <v-snackbar v-model="feedback.show" :color="feedback.success ? 'primary' : 'error'" timeout="3000" location="bottom right">
      {{ feedback.text }}
    </v-snackbar>
  </v-card>
</template>
