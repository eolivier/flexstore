<script setup lang="ts">
  import { computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSocialNetworksStore } from '@/stores/socialNetworks.store';
  import { givenSocialNetworks, socialNetworkIcon, socialNetworkLink } from '@/utils/socialNetworks';

  const { t } = useI18n();
  const socialNetworksStore = useSocialNetworksStore();

  // Only the networks where the shop has an account
  const networks = computed(() => givenSocialNetworks(socialNetworksStore.socialNetworkUrls));

  onMounted(() => {
    socialNetworksStore.fetchSocialNetworks();
  });
</script>

<template>
  <div v-if="networks.length > 0" class="d-flex align-center flex-wrap ga-1 mt-4" :aria-label="t('socialNetworks.followUs')">
    <v-btn
      v-for="socialNetwork in networks"
      :key="socialNetwork"
      :href="socialNetworkLink(socialNetworksStore.socialNetworkUrls[socialNetwork])"
      :icon="socialNetworkIcon(socialNetwork)"
      :aria-label="t(`socialNetworks.${socialNetwork}`)"
      :data-social-network="socialNetwork"
      target="_blank"
      rel="noopener noreferrer"
      variant="text"
      size="small"
    />
  </div>
</template>
