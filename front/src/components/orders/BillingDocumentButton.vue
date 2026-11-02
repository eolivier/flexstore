<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { JsonOrder } from '@/generated';
  import { useAuthStore } from '@/stores/auth.store';
  import { orderNumber } from '@/utils/order';
  import { saveFile } from '@/utils/download';

  // The documents of the invoicing of an order: its invoice once paid, its credit note once refunded
  export type BillingDocument = 'invoice' | 'creditNote';

  // Downloads a document of an order (a PDF); ordersPath: where the orders are read, by their customer
  // (/api/orders) or by an administrator (/api/admin/orders)
  const props = defineProps<{ order: JsonOrder; ordersPath: string; document: BillingDocument }>();

  const { t } = useI18n();
  const authStore = useAuthStore();

  const downloading = ref(false);
  const failed = ref(false);

  const documentPath = computed(() => {
    const documentName = props.document === 'invoice' ? 'invoice' : 'credit-note';
    return `${props.ordersPath}/${encodeURIComponent(props.order.id)}/${documentName}`;
  });
  // The name the shop gives the file: facture-<number>.pdf, or avoir-<number>.pdf
  const fileName = computed(() => {
    const prefix = props.document === 'invoice' ? 'facture' : 'avoir';
    return `${prefix}-${orderNumber(props.order)}.pdf`;
  });

  async function download() {
    downloading.value = true;
    failed.value = false;
    const response = await authStore.authFetch(documentPath.value).catch(() => null);
    if (response?.ok) {
      saveFile(await response.blob(), fileName.value);
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
      {{ t(`orders.billingDocuments.${document}.download`) }}
    </v-btn>
    <span v-if="failed" class="text-caption text-error mt-1">
      {{ t(`orders.billingDocuments.${document}.error`) }}
    </span>
  </div>
</template>
