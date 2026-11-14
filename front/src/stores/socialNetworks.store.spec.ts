import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { noSocialNetworkUrls } from '@/utils/socialNetworks';
import { useSocialNetworksStore } from './socialNetworks.store';

const getSocialNetworks = vi.fn();
vi.mock('@/api', () => ({ sellerApi: { getSocialNetworks: () => getSocialNetworks() } }));

describe('socialNetworks.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    getSocialNetworks.mockReset();
  });

  it('should know no account before the shop answers', () => {
    // Act
    const store = useSocialNetworksStore();
    // Assert
    expect(store.socialNetworkUrls).toEqual(noSocialNetworkUrls());
  });

  it('should keep the accounts given by the shop', async () => {
    // Arrange
    const socialNetworkUrls = { ...noSocialNetworkUrls(), instagram: 'https://www.instagram.com/flexstore' };
    getSocialNetworks.mockResolvedValue({ data: socialNetworkUrls });
    const store = useSocialNetworksStore();
    // Act
    await store.fetchSocialNetworks();
    // Assert
    expect(store.socialNetworkUrls).toEqual(socialNetworkUrls);
  });

  it('should keep no account when the shop cannot be reached', async () => {
    // Arrange
    getSocialNetworks.mockRejectedValue(new Error('offline'));
    const store = useSocialNetworksStore();
    // Act
    await store.fetchSocialNetworks();
    // Assert
    expect(store.socialNetworkUrls).toEqual(noSocialNetworkUrls());
  });
});
