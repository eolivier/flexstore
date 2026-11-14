import { useI18n } from 'vue-i18n';
import { isEmail } from '../utils/email';

const MINIMUM_PASSWORD_LENGTH = 8;

// The rules of the fields shared by the forms, with the messages shown under a field that breaks one
export function useFieldRules() {
  const { t } = useI18n();

  return {
    required: (value: string | null) => !!value?.trim() || t('rule.required'),
    email: (value: string) => isEmail(value) || t('rule.invalidEmail'),
    passwordLength: (value: string) => value.length >= MINIMUM_PASSWORD_LENGTH || t('rule.passwordTooShort'),
  };
}
