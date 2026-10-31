<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSellerStore } from '@/stores/seller.store';
  import SellerForm from './SellerForm.vue';

  const { t } = useI18n();
  const sellerStore = useSellerStore();

  onMounted(() => {
    sellerStore.fetchSeller();
  });
</script>

<template>
  <v-container class="page py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('seller.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-8">{{ t('seller.subtitle') }}</p>

    <v-alert v-if="sellerStore.loadError" type="error" variant="tonal">{{ t('seller.loadError') }}</v-alert>

    <v-skeleton-loader v-else-if="!sellerStore.seller" type="list-item-two-line" class="rounded-xl" />

    <v-card v-else class="pa-6">
      <SellerForm :seller="sellerStore.seller" />
      <p class="text-body-2 text-medium-emphasis mt-4">{{ t('seller.issuedInvoicesNote') }}</p>
    </v-card>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
</style>
