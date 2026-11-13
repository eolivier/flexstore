import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import FooterSocialNetworks from './FooterSocialNetworks.vue';
import { noSocialNetworkUrls } from '@/utils/socialNetworks';
import { vuetify, i18n } from '@/test/setup';

const mockGetSocialNetworks = vi.fn();
vi.mock('@/api', () => ({ sellerApi: { getSocialNetworks: () => mockGetSocialNetworks() } }));

async function mountFooterSocialNetworks() {
  const wrapper = mount(FooterSocialNetworks, { global: { plugins: [vuetify, i18n] } });
  await flushPromises();
  return wrapper;
}

const networksShown = (wrapper: Awaited<ReturnType<typeof mountFooterSocialNetworks>>) =>
  wrapper.findAll('a').map((link) => link.attributes('data-social-network'));

describe('FooterSocialNetworks.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockGetSocialNetworks.mockReset();
  });

  it('should show nothing when the shop has no social network', async () => {
    // Arrange
    mockGetSocialNetworks.mockResolvedValueOnce({ data: noSocialNetworkUrls() });
    // Act
    const wrapper = await mountFooterSocialNetworks();
    // Assert
    expect(networksShown(wrapper)).toEqual([]);
  });

  it('should show only the social networks the shop gave, in the usual order', async () => {
    // Arrange
    const socialNetworkUrls = {
      ...noSocialNetworkUrls(),
      twitch: 'https://www.twitch.tv/flexstore',
      facebook: 'https://www.facebook.com/flexstore',
      tiktok: '   ',
    };
    mockGetSocialNetworks.mockResolvedValueOnce({ data: socialNetworkUrls });
    // Act
    const wrapper = await mountFooterSocialNetworks();
    // Assert
    expect(networksShown(wrapper)).toEqual(['facebook', 'twitch']);
  });

  it('should open an address given without scheme on the web, in a new tab', async () => {
    // Arrange
    mockGetSocialNetworks.mockResolvedValueOnce({ data: { ...noSocialNetworkUrls(), x: 'x.com/flexstore' } });
    // Act
    const wrapper = await mountFooterSocialNetworks();
    // Assert
    const link = wrapper.get('a');
    expect(link.attributes('href')).toBe('https://x.com/flexstore');
    expect(link.attributes('target')).toBe('_blank');
    expect(link.attributes('rel')).toContain('noopener');
    expect(link.attributes('aria-label')).toBe('X');
  });

  it('should show nothing when the shop cannot be reached', async () => {
    // Arrange
    mockGetSocialNetworks.mockRejectedValueOnce(new Error('network'));
    // Act
    const wrapper = await mountFooterSocialNetworks();
    // Assert
    expect(networksShown(wrapper)).toEqual([]);
  });
});
