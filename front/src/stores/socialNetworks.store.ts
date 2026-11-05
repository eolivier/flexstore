import { defineStore } from 'pinia';
import { ref } from 'vue';
import { sellerApi } from '@/api';
import { noSocialNetworkUrls, type SocialNetworkUrls } from '@/utils/socialNetworks';

// The accounts of the shop on the social networks, read by everyone: none until the shop answers, and none when
// it cannot be reached
export const useSocialNetworksStore = defineStore('socialNetworks', () => {
  const socialNetworkUrls = ref<SocialNetworkUrls>(noSocialNetworkUrls());

  async function fetchSocialNetworks() {
    const response = await sellerApi.getSocialNetworks().catch(() => null);
    if (response) {
      socialNetworkUrls.value = response.data;
    }
  }

  return { socialNetworkUrls, fetchSocialNetworks };
});
