<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import type { SocialNetworkUrls } from '@/utils/socialNetworks';
  import SellerSocialNetworksFields from '../SellerSocialNetworksFields.vue';

  const socialNetworkUrls = defineModel<SocialNetworkUrls>('socialNetworks', { required: true });

  const { t } = useI18n();
</script>

<template>
  <div>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t('sellerSetup.socialNetworksText') }}</p>
    <SellerSocialNetworksFields v-model="socialNetworkUrls" />
  </div>
</template>
