<script setup lang="ts">
  import LoadError from '@/components/layout/LoadError.vue';
  import { computed, onMounted, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useLegalPagesStore } from '@/stores/legalPages.store';

  // The customer accepts the current version of the terms of sale, which they can read in another tab: the model
  // is the id of that version once the box is ticked, null otherwise
  const acceptedTermsOfSaleId = defineModel<string | null>({ required: true });

  const { t } = useI18n();
  const legalPagesStore = useLegalPagesStore();
  const accepted = ref(false);
  const termsOfSale = computed(() => legalPagesStore.legalPages['terms-of-sale']);
  const loadError = computed(() => legalPagesStore.loadErrors['terms-of-sale'] === true);

  onMounted(() => legalPagesStore.fetchLegalPage('terms-of-sale'));

  watch([accepted, termsOfSale], ([isAccepted, currentTermsOfSale]) => {
    acceptedTermsOfSaleId.value = isAccepted && currentTermsOfSale ? currentTermsOfSale.id : null;
  });
</script>

<template>
  <div class="terms-of-sale">
    <v-checkbox v-model="accepted" :label="t('checkout.termsOfSale.accept')" :disabled="!termsOfSale" hide-details />
    <router-link to="/terms-of-sale" target="_blank" class="text-body-2">
      {{ t('checkout.termsOfSale.read') }}
    </router-link>
    <LoadError v-if="loadError" :message="t('checkout.termsOfSale.loadError')" density="compact" class="mt-2" />
  </div>
</template>
