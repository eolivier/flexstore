// The features presented on the About page, grouped by audience or by trigger;
// the texts are in `about.features.<group>.<key>`
export interface AboutFeature {
  key: string;
  icon: string;
}

export interface AboutFeatureGroup {
  key: string;
  icon: string;
  color: string;
  features: AboutFeature[];
}

export const aboutFeatureGroups: AboutFeatureGroup[] = [
  {
    key: 'shopping',
    icon: 'mdi-storefront-outline',
    color: 'primary',
    features: [
      { key: 'catalog', icon: 'mdi-view-grid-outline' },
      { key: 'cart', icon: 'mdi-cart-outline' },
      { key: 'checkout', icon: 'mdi-credit-card-outline' },
      { key: 'reviews', icon: 'mdi-star-outline' },
    ],
  },
  {
    key: 'account',
    icon: 'mdi-account-circle-outline',
    color: 'secondary',
    features: [
      { key: 'profile', icon: 'mdi-account-outline' },
      { key: 'orders', icon: 'mdi-truck-outline' },
      { key: 'addresses', icon: 'mdi-map-marker-outline' },
      { key: 'wishlist', icon: 'mdi-heart-outline' },
      { key: 'loyalty', icon: 'mdi-star-circle-outline' },
      { key: 'googleSignIn', icon: 'mdi-google' },
      { key: 'security', icon: 'mdi-shield-lock-outline' },
      { key: 'twoFactorAuthentication', icon: 'mdi-two-factor-authentication' },
    ],
  },
  {
    key: 'admin',
    icon: 'mdi-shield-crown-outline',
    color: 'warning',
    features: [
      { key: 'sales', icon: 'mdi-chart-line' },
      { key: 'users', icon: 'mdi-account-multiple-outline' },
      { key: 'products', icon: 'mdi-package-variant-closed' },
      { key: 'orders', icon: 'mdi-truck-delivery-outline' },
      { key: 'productPromotions', icon: 'mdi-label-percent-outline' },
      { key: 'discountCodes', icon: 'mdi-ticket-percent-outline' },
      { key: 'seller', icon: 'mdi-store-outline' },
      { key: 'legalPages', icon: 'mdi-scale-balance' },
      { key: 'settings', icon: 'mdi-cog-outline' },
      { key: 'environment', icon: 'mdi-console' },
      { key: 'sellerSetup', icon: 'mdi-auto-fix' },
    ],
  },
  {
    key: 'automatic',
    icon: 'mdi-robot-outline',
    color: 'info',
    features: [
      { key: 'unpaidOrders', icon: 'mdi-timer-sand' },
      { key: 'abandonedCart', icon: 'mdi-cart-arrow-right' },
    ],
  },
  {
    key: 'assistants',
    icon: 'mdi-creation-outline',
    color: 'success',
    features: [{ key: 'mcp', icon: 'mdi-connection' }],
  },
];
