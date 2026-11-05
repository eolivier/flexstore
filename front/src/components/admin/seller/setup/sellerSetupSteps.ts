import type { Seller } from '@/stores/seller.store';
import {
  isEmail,
  isPhone,
  isShareCapital,
  isSiret,
  isSocialNetworkUrl,
  isVatNumber,
  isWebsite,
} from '../sellerRules';
import { SOCIAL_NETWORKS } from '@/utils/socialNetworks';

// The steps of the setup assistant of the seller, in the order they are asked
export const SELLER_SETUP_STEPS = ['identity', 'registration', 'contact', 'socialNetworks', 'summary'] as const;

export type SellerSetupStep = (typeof SELLER_SETUP_STEPS)[number];

const isGiven = (text: string) => text.trim().length > 0;

// Who the seller is: its name, its address, its legal form and its share capital
function isIdentityComplete(seller: Seller) {
  const isCompanyGiven = isGiven(seller.name) && isGiven(seller.address) && isGiven(seller.legalForm);
  return isCompanyGiven && isShareCapital(seller.shareCapital);
}

// How the seller is registered: its SIRET, the city of its register and its VAT number
function isRegistrationComplete(seller: Seller) {
  return isSiret(seller.siret) && isGiven(seller.registryCity) && isVatNumber(seller.vatNumber);
}

// How the customers reach the seller: its phone number, its email and its website
function isContactComplete(seller: Seller) {
  return isPhone(seller.phone) && isEmail(seller.email) && isWebsite(seller.website);
}

// Where the customers find the shop on the social networks: each address is optional, but a given one must be
// a web address
function isSocialNetworksComplete(seller: Seller) {
  return SOCIAL_NETWORKS.every((socialNetwork) => isSocialNetworkUrl(seller.socialNetworks[socialNetwork]));
}

// A step is complete when the seller satisfies its rules: the next one can be opened
export function isStepComplete(step: SellerSetupStep, seller: Seller) {
  if (step === 'identity') return isIdentityComplete(seller);
  if (step === 'registration') return isRegistrationComplete(seller);
  if (step === 'contact') return isContactComplete(seller);
  if (step === 'socialNetworks') return isSocialNetworksComplete(seller);
  const isSellerComplete = isIdentityComplete(seller) && isRegistrationComplete(seller);
  return isSellerComplete && isContactComplete(seller) && isSocialNetworksComplete(seller);
}
