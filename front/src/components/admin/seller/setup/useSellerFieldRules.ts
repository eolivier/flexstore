import { useI18n } from 'vue-i18n';
import { isEmail, isPhone, isShareCapital, isSiret, isVatNumber, isWebsite } from '../sellerRules';

// The rules of the fields of the seller, with the messages shown under a field that breaks one
export function useSellerFieldRules() {
  const { t } = useI18n();

  return {
    required: (value: string) => value.trim().length > 0 || t('seller.required'),
    siret: (value: string) => isSiret(value) || t('seller.siretFormat'),
    vatNumber: (value: string) => isVatNumber(value) || t('seller.vatNumberFormat'),
    phone: (value: string) => isPhone(value) || t('seller.phoneFormat'),
    email: (value: string) => isEmail(value) || t('seller.emailFormat'),
    website: (value: string) => isWebsite(value) || t('seller.websiteFormat'),
    shareCapital: (value: string) => isShareCapital(value) || t('seller.shareCapitalFormat'),
  };
}
