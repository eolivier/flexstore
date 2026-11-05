// The social networks where the shop may have an account, in the order they are asked and shown
export const SOCIAL_NETWORKS = ['facebook', 'instagram', 'x', 'tiktok', 'youtube', 'twitch'] as const;

export type SocialNetwork = (typeof SOCIAL_NETWORKS)[number];

// The web address of the account of the shop on each network: empty where the shop has none
export type SocialNetworkUrls = Record<SocialNetwork, string>;

// X and TikTok have no icon in the Material Design Icons font: their logos are drawn from an SVG path
const X_LOGO =
  'svg:M18.901 1.153h3.68l-8.04 9.19L24 22.846h-7.406l-5.8-7.584-6.638 7.584H.474l8.6-9.83L0 1.154h7.594l5.243 6.932ZM17.61 20.644h2.039L6.486 3.24H4.298Z';
const TIKTOK_LOGO =
  'svg:M12.525.02c1.31-.02 2.61-.01 3.91-.02.08 1.53.63 3.09 1.75 4.17 1.12 1.11 2.7 1.62 4.24 1.79v4.03c-1.44-.05-2.89-.35-4.2-.97-.57-.26-1.1-.59-1.62-.93-.01 2.92.01 5.84-.02 8.75-.08 1.4-.54 2.79-1.35 3.94-1.31 1.92-3.58 3.17-5.91 3.21-1.43.08-2.86-.31-4.08-1.03-2.02-1.19-3.44-3.37-3.65-5.71-.02-.5-.03-1-.01-1.49.18-1.9 1.12-3.72 2.58-4.96 1.66-1.44 3.98-2.13 6.15-1.72.02 1.48-.04 2.96-.04 4.44-.99-.32-2.15-.23-3.02.37-.63.41-1.11 1.04-1.36 1.75-.21.51-.15 1.07-.14 1.61.24 1.64 1.82 3.02 3.5 2.87 1.12-.01 2.19-.66 2.77-1.61.19-.33.4-.67.41-1.06.1-1.79.06-3.57.07-5.36.01-4.03-.01-8.05.02-12.07z';

const ICONS: Record<SocialNetwork, string> = {
  facebook: 'mdi-facebook',
  instagram: 'mdi-instagram',
  x: X_LOGO,
  tiktok: TIKTOK_LOGO,
  youtube: 'mdi-youtube',
  twitch: 'mdi-twitch',
};

export const socialNetworkIcon = (socialNetwork: SocialNetwork) => ICONS[socialNetwork];

// No account anywhere: the shop has not given any network
export function noSocialNetworkUrls(): SocialNetworkUrls {
  return { facebook: '', instagram: '', x: '', tiktok: '', youtube: '', twitch: '' };
}

// The networks where the shop has an account, in the order they are shown
export function givenSocialNetworks(urls: SocialNetworkUrls) {
  return SOCIAL_NETWORKS.filter((socialNetwork) => urls[socialNetwork].trim().length > 0);
}

// The address as a link the browser follows: without scheme, it is on the web (https)
export function socialNetworkLink(url: string) {
  const address = url.trim();
  return /^https?:\/\//i.test(address) ? address : `https://${address}`;
}
