<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import QRCode from 'qrcode';
  import { useAuthStore } from '@/stores/auth.store';
  import FormDialog from '@/components/layout/FormDialog.vue';
  import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';

  const open = defineModel<boolean>({ required: true });
  const emit = defineEmits<{ enabled: [] }>();
  const { t } = useI18n();
  const authStore = useAuthStore();

  const secret = ref('');
  const qrCodeDataUrl = ref('');
  const code = ref('');
  const loading = ref(false);
  const error = ref('');

  // The enrollment starts as soon as the dialog opens: a new secret, kept pending until confirmed
  watch(open, async (isOpen) => {
    if (!isOpen) return;
    error.value = '';
    code.value = '';
    qrCodeDataUrl.value = '';
    const enrollment = await authStore.startTwoFactorEnrollment();
    if (enrollment) {
      secret.value = enrollment.secret;
      qrCodeDataUrl.value = await QRCode.toDataURL(enrollment.provisioningUri);
    } else {
      error.value = t('twoFactorAuth.enrollmentError');
    }
  });

  async function confirm() {
    loading.value = true;
    const outcome = await authStore.confirmTwoFactorEnrollment(code.value);
    loading.value = false;
    if (outcome === 'confirmed') {
      open.value = false;
      emit('enabled');
    } else {
      error.value = outcome === 'invalidCode' ? t('twoFactorAuth.invalidCode') : t('twoFactorAuth.enrollmentError');
    }
  }
</script>

<template>
  <FormDialog v-model="open" :title="t('twoFactorAuth.confirmTitle')" @submit="confirm">
    <template v-if="qrCodeDataUrl">
      <p class="text-body-2 mb-3">{{ t('twoFactorAuth.scanQrCode') }}</p>
      <div class="d-flex justify-center mb-3">
        <img :src="qrCodeDataUrl" alt="QR code" width="200" height="200" />
      </div>
      <p class="text-body-2 text-medium-emphasis mb-1">{{ t('twoFactorAuth.manualSecret') }}</p>
      <p class="text-body-2 font-weight-medium mb-4" style="word-break: break-all">{{ secret }}</p>
      <p class="text-body-2 mb-3">{{ t('twoFactorAuth.confirmText') }}</p>
      <v-text-field
        v-model="code"
        :label="t('twoFactorAuth.codeLabel')"
        inputmode="numeric"
        maxlength="6"
        class="mb-1"
      />
      <FeedbackAlert v-if="error" type="error" compact class="mb-4">{{ error }}</FeedbackAlert>
    </template>
    <FeedbackAlert v-else-if="error" type="error" compact>{{ error }}</FeedbackAlert>
    <div v-else class="d-flex justify-center pa-6">
      <v-progress-circular indeterminate color="primary" />
    </div>
    <template v-if="qrCodeDataUrl" #actions>
      <v-btn type="submit" color="primary" :loading="loading" :disabled="code.length !== 6">
        {{ t('twoFactorAuth.confirm') }}
      </v-btn>
    </template>
  </FormDialog>
</template>
