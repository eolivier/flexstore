// A section of the shop that the guided tour presents: its icon, the prefix of its texts (title and text) and
// the name of the part of the page it brings into focus (the value of its `data-guided-tour` attribute) and the
// menu to open so that this part is displayed, if any
export interface GuidedTourSection {
  icon: string;
  messageKey: string;
  focus: string;
  menu?: GuidedTourMenu;
}

export type GuidedTourMenu = 'account';

// The sections of the shop the guided tour goes through, in order
export const guidedTourSections: GuidedTourSection[] = [
  { icon: 'mdi-view-grid-outline', messageKey: 'guidedTour.sections.catalog', focus: 'shop' },
  { icon: 'mdi-cart-outline', messageKey: 'guidedTour.sections.cart', focus: 'cart' },
  { icon: 'mdi-truck-outline', messageKey: 'guidedTour.sections.orders', focus: '/orders', menu: 'account' },
  { icon: 'mdi-heart-outline', messageKey: 'guidedTour.sections.wishlist', focus: '/account/wishlist', menu: 'account' },
  { icon: 'mdi-star-circle-outline', messageKey: 'guidedTour.sections.loyalty', focus: '/account/loyalty', menu: 'account' },
  { icon: 'mdi-tune-variant', messageKey: 'guidedTour.sections.preferences', focus: '/account/preferences', menu: 'account' },
];

// NONE: the account holds no choice yet (the tour is shown then)
export type AccountGuidedTour = 'shown' | 'hidden' | 'none';

export const guidedTourOfAccount: Record<string, AccountGuidedTour> = {
  SHOWN: 'shown',
  HIDDEN: 'hidden',
  NONE: 'none',
};
