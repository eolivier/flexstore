<script setup lang="ts">
  import LoadError from '@/components/layout/LoadError.vue';
  import { onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from 'vue-i18n';
  import { useSellerStore, type Seller, type SellerUpdate } from '@/stores/seller.store';
  import { useSettingsStore } from '@/stores/settings.store';
  import SellerSetupStepper from './SellerSetupStepper.vue';

  const { t } = useI18n();
  const router = useRouter();
  const sellerStore = useSellerStore();
  const settingsStore = useSettingsStore();

  const saving = ref(false);
  // null until the shop answers
  const outcome = ref<SellerUpdate | null>(null);

  onMounted(() => {
    sellerStore.fetchSeller();
  });

  // Once the seller is saved, the seller setup of the settings is on: the administration opens
  async function finish(givenSeller: Seller) {
    saving.value = true;
    outcome.value = await sellerStore.completeSetup(givenSeller);
    if (outcome.value === 'saved') {
      await settingsStore.fetchSettings();
      await router.push('/admin');
    }
    saving.value = false;
  }
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('sellerSetup.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-8">{{ t('sellerSetup.subtitle') }}</p>

    <LoadError v-if="sellerStore.loadError" :message="t('seller.loadError')" />

    <v-skeleton-loader v-else-if="!sellerStore.seller" type="list-item-two-line" class="rounded-xl" />

    <SellerSetupStepper
      v-else
      :seller="sellerStore.seller"
      :saving="saving"
      :refusal="outcome === 'saved' ? null : outcome"
      @finish="finish"
    />
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 960px;
  }
</style>
