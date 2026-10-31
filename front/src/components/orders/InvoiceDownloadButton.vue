<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { useAuthStore } from '@/stores/auth.store';
  import { orderNumber } from '@/utils/order';
  import { saveFile } from '@/utils/download';

  // Downloads the invoice of a paid order (a PDF); invoicePath: the address of the invoice, for its customer or
  // for an administrator
  const props = defineProps<{ order: JsonOrder; invoicePath: string }>();

  const { t } = useI18n();
  const authStore = useAuthStore();

  const downloading = ref(false);
  const failed = ref(false);

  async function download() {
    downloading.value = true;
    failed.value = false;
    const response = await authStore.authFetch(props.invoicePath).catch(() => null);
    if (response?.ok) {
      saveFile(await response.blob(), `facture-${orderNumber(props.order)}.pdf`);
    } else {
      failed.value = true;
    }
    downloading.value = false;
  }
</script>

<template>
  <div class="d-flex flex-column align-end">
    <v-btn
      :loading="downloading"
      variant="tonal"
      color="primary"
      size="small"
      prepend-icon="mdi-file-download-outline"
      @click="download"
    >
      {{ t('orders.downloadInvoice') }}
    </v-btn>
    <span v-if="failed" class="text-caption text-error mt-1">{{ t('orders.invoiceError') }}</span>
  </div>
</template>
