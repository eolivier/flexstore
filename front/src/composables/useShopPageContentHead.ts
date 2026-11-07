import { computed, onMounted } from 'vue';
import { useHead } from '@unhead/vue';
import { useCookieConsentStore } from '@/stores/cookieConsent.store';
import { useShopPageContentStore } from '@/stores/shopPageContent.store';

const ANALYTICS_IDENTIFIER = /^[A-Za-z0-9-]{1,50}$/;

// The script of Google Analytics for an identifier, as Google gives it
function analyticsScripts(analyticsId: string) {
  const measure = `window.dataLayer=window.dataLayer||[];function gtag(){dataLayer.push(arguments);}gtag('js',new Date());gtag('config','${analyticsId}');`;
  return [
    { src: `https://www.googletagmanager.com/gtag/js?id=${analyticsId}`, async: true },
    { innerHTML: measure },
  ];
}

// What the administrators wrote for the head of every page: the key words, and the Google Analytics script,
// loaded only for a visitor who accepted the cookies and for an identifier written as such. The contents are
// read once the page is in the browser
export function useShopPageContentHead() {
  const shopPageContentStore = useShopPageContentStore();
  const cookieConsentStore = useCookieConsentStore();

  const analyticsId = computed(() => {
    const identifier = shopPageContentStore.textOf('GOOGLE_ANALYTICS_ID');
    return ANALYTICS_IDENTIFIER.test(identifier) && cookieConsentStore.preferencesAccepted ? identifier : '';
  });

  onMounted(() => {
    shopPageContentStore.fetchContents();
  });

  useHead(() => {
    const keyWords = shopPageContentStore.textOf('META_KEY_WORDS');
    return {
      meta: keyWords ? [{ name: 'keywords', content: keyWords }] : [],
      script: analyticsId.value ? analyticsScripts(analyticsId.value) : [],
    };
  });
}
