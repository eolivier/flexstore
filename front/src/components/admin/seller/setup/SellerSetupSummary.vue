<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Seller } from '@/stores/seller.store';
  import { givenSocialNetworks } from '@/utils/socialNetworks';

  const props = defineProps<{ seller: Seller }>();

  const { t } = useI18n();

  // The social networks the shop gave: the ones it did not give are not listed
  const socialNetworkLines = computed(() =>
    givenSocialNetworks(props.seller.socialNetworks).map((socialNetwork) => ({
      label: `socialNetworks.${socialNetwork}`,
      value: props.seller.socialNetworks[socialNetwork],
    }))
  );

  const lines = computed(() => [
    { label: 'seller.name', value: props.seller.name },
    { label: 'seller.address', value: props.seller.address },
    { label: 'seller.legalForm', value: props.seller.legalForm },
    { label: 'seller.shareCapital', value: props.seller.shareCapital },
    { label: 'seller.siret', value: props.seller.siret },
    { label: 'seller.registryCity', value: props.seller.registryCity },
    { label: 'seller.vatNumber', value: props.seller.vatNumber },
    { label: 'seller.phone', value: props.seller.phone },
    { label: 'seller.email', value: props.seller.email },
    { label: 'seller.website', value: props.seller.website },
    ...socialNetworkLines.value,
  ]);
</script>

<template>
  <div>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t('sellerSetup.summaryText') }}</p>
    <v-list lines="one" density="compact">
      <v-list-item v-for="line in lines" :key="line.label" :title="line.value" :subtitle="t(line.label)" />
    </v-list>
  </div>
</template>
