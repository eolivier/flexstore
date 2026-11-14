import { describe, expect, it } from 'vitest';
import {
  SOCIAL_NETWORKS,
  givenSocialNetworks,
  noSocialNetworkUrls,
  socialNetworkExampleAddress,
  socialNetworkIcon,
  socialNetworkLink,
} from './socialNetworks';

describe('socialNetworks', () => {
  it.each(SOCIAL_NETWORKS)('%s should have an icon', (socialNetwork) => {
    // Act
    const icon = socialNetworkIcon(socialNetwork);
    // Assert
    expect(icon).toBeTruthy();
  });

  it.each([
    { socialNetwork: 'facebook', expected: 'mdi-facebook' },
    { socialNetwork: 'x', expected: 'svg:' },
    { socialNetwork: 'tiktok', expected: 'svg:' },
  ] as const)('the icon of $socialNetwork should start with $expected', ({ socialNetwork, expected }) => {
    // Act
    const icon = socialNetworkIcon(socialNetwork);
    // Assert
    expect(icon.startsWith(expected)).toBe(true);
  });

  it.each(SOCIAL_NETWORKS)('%s should have an example address on the web', (socialNetwork) => {
    // Act
    const exampleAddress = socialNetworkExampleAddress(socialNetwork);
    // Assert
    expect(exampleAddress).toMatch(/^https:\/\/.+\/@?flexstore$/);
  });

  it('should have no account on any network when none is given', () => {
    // Act
    const socialNetworkUrls = noSocialNetworkUrls();
    // Assert
    expect(Object.keys(socialNetworkUrls)).toEqual([...SOCIAL_NETWORKS]);
    expect(Object.values(socialNetworkUrls).every((url) => url === '')).toBe(true);
  });

  it('should return a new object each time no account is given', () => {
    // Act
    const first = noSocialNetworkUrls();
    const second = noSocialNetworkUrls();
    // Assert
    expect(first).not.toBe(second);
  });

  it('should list the networks that have an account, in the order they are shown', () => {
    // Arrange
    const twitchAndFacebook = { twitch: 'twitch.tv/flexstore', facebook: 'fb.com/flexstore' };
    const socialNetworkUrls = { ...noSocialNetworkUrls(), ...twitchAndFacebook };
    // Act
    const socialNetworks = givenSocialNetworks(socialNetworkUrls);
    // Assert
    expect(socialNetworks).toEqual(['facebook', 'twitch']);
  });

  it('should ignore a network whose address is blank', () => {
    // Arrange
    const socialNetworkUrls = { ...noSocialNetworkUrls(), x: '   ' };
    // Act
    const socialNetworks = givenSocialNetworks(socialNetworkUrls);
    // Assert
    expect(socialNetworks).toEqual([]);
  });

  it.each(['https://x.com/flexstore', 'http://x.com/flexstore', 'HTTPS://x.com/flexstore', ' https://x.com/flexstore '])(
    'the address "%s" should be followed as it is, trimmed',
    (address) => {
      // Act
      const link = socialNetworkLink(address);
      // Assert
      expect(link).toBe(address.trim());
    }
  );

  it('should put an address without scheme on the web', () => {
    // Act
    const link = socialNetworkLink(' x.com/flexstore ');
    // Assert
    expect(link).toBe('https://x.com/flexstore');
  });
});
