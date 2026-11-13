// A section of the shop that the guided tour presents: its icon and the prefix of its texts (title and text)
export interface GuidedTourSection {
  icon: string;
  messageKey: string;
}

// The sections of the shop the guided tour goes through, in order
export const guidedTourSections: GuidedTourSection[] = [
  { icon: 'mdi-view-grid-outline', messageKey: 'guidedTour.sections.catalog' },
  { icon: 'mdi-cart-outline', messageKey: 'guidedTour.sections.cart' },
  { icon: 'mdi-truck-outline', messageKey: 'guidedTour.sections.orders' },
  { icon: 'mdi-heart-outline', messageKey: 'guidedTour.sections.wishlist' },
  { icon: 'mdi-star-circle-outline', messageKey: 'guidedTour.sections.loyalty' },
  { icon: 'mdi-tune-variant', messageKey: 'guidedTour.sections.preferences' },
];

// NONE: the account holds no choice yet (the tour is shown then)
export type AccountGuidedTour = 'shown' | 'hidden' | 'none';

export const guidedTourOfAccount: Record<string, AccountGuidedTour> = {
  SHOWN: 'shown',
  HIDDEN: 'hidden',
  NONE: 'none',
};
