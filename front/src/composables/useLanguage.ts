import { computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useLanguageStore } from '@/stores/language.store';
import { isShopLanguage, isTranslatedPath, localizedPath, MAIN_LANGUAGE, type ShopLanguage } from '@/utils/language';

// The language of the shop, changed by the customer and kept in their account once signed in (where it applies
// as soon as they sign in)
export function useLanguage() {
  const { locale } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const languageStore = useLanguageStore();
  const language = computed<ShopLanguage>(() => (isShopLanguage(locale.value) ? locale.value : MAIN_LANGUAGE));

  // A page that exists in each language moves to the address of the new one
  function apply(newLanguage: ShopLanguage) {
    locale.value = newLanguage;
    if (isTranslatedPath(route.path)) {
      router.push({ path: localizedPath(route.path, newLanguage), query: route.query });
    }
  }

  function changeLanguage(newLanguage: ShopLanguage) {
    apply(newLanguage);
    languageStore.choose(newLanguage);
  }

  watch(
    () => languageStore.accountLanguage,
    (accountLanguage) => {
      if (accountLanguage !== 'none' && accountLanguage !== language.value) apply(accountLanguage);
    },
    { immediate: true }
  );

  return { language, changeLanguage };
}
