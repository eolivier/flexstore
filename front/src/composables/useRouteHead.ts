import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { usePageHead } from '@/composables/usePageHead';
import { useShopPageContentStore } from '@/stores/shopPageContent.store';
import { SITE_NAME } from '@/utils/seo';

// The head of the pages whose title and description are fixed (route meta); a page that depends on its data
// (a product, a category) registers its own head with usePageHead, which then wins
export function useRouteHead() {
  const { t } = useI18n();
  const route = useRoute();
  const shopPageContentStore = useShopPageContentStore();

  // The description of the shop, written by the administrators, comes before the default one of the site
  function description() {
    if (route.meta.description) return t(route.meta.description);
    return shopPageContentStore.textOf('META_SHOP_DESCRIPTION') || t('seo.siteDescription');
  }

  usePageHead(() => ({
    title: route.meta.title ? t(route.meta.title) : SITE_NAME,
    description: description(),
    path: route.path,
    indexed: route.meta.indexed === true,
    translated: route.meta.indexed === true,
    imageUrl: null,
    structuredData: null,
  }));
}
