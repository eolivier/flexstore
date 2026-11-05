import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { usePageHead } from '@/composables/usePageHead';
import { SITE_NAME } from '@/utils/seo';

// The head of the pages whose title and description are fixed (route meta); a page that depends on its data
// (a product, a category) registers its own head with usePageHead, which then wins
export function useRouteHead() {
  const { t } = useI18n();
  const route = useRoute();

  usePageHead(() => ({
    title: route.meta.title ? t(route.meta.title) : SITE_NAME,
    description: t(route.meta.description ?? 'seo.siteDescription'),
    path: route.path,
    indexed: route.meta.indexed === true,
    translated: route.meta.indexed === true,
    imageUrl: null,
    structuredData: null,
  }));
}
