<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { SOCIAL_NETWORKS, socialNetworkIcon, type SocialNetworkUrls } from '@/utils/socialNetworks';
  import { isSocialNetworkUrl } from './sellerRules';

  // The address of the account of the shop on each social network, each one optional
  const socialNetworkUrls = defineModel<SocialNetworkUrls>({ required: true });

  const { t } = useI18n();

  const isWebAddress = (url: string) => isSocialNetworkUrl(url) || t('socialNetworks.format');
</script>

<template>
  <div>
    <v-text-field
      v-for="socialNetwork in SOCIAL_NETWORKS"
      :key="socialNetwork"
      v-model="socialNetworkUrls[socialNetwork]"
      :label="t(`socialNetworks.${socialNetwork}`)"
      :hint="t('socialNetworks.hint')"
      :rules="[isWebAddress]"
      :prepend-inner-icon="socialNetworkIcon(socialNetwork)"
      class="mb-2"
    />
  </div>
</template>
