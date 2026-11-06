import { useHead } from '@unhead/vue';
import { useI18n } from 'vue-i18n';
import { canonicalUrl, pageTitle, SITE_NAME } from '@/utils/seo';
import { localizedPath, MAIN_LANGUAGE, type ShopLanguage } from '@/utils/language';

// What a page tells the search engines and the social networks about itself
export interface PageHead {
  title: string;
  description: string;
  path: string;
  indexed: boolean;
  // The address of the page in the other language follows from its own: the head links the two (hreflang)
  translated: boolean;
  imageUrl: string | null;
  structuredData: object | null;
}

// The head of the page follows the values the getter reads; the page that registers it last wins
// The address of the page in each language, and the one for the visitors of any other language
function languageAlternates(path: string) {
  const alternate = (hreflang: string, language: ShopLanguage) => ({
    rel: 'alternate' as const,
    hreflang,
    href: canonicalUrl(localizedPath(path, language)),
  });
  return [alternate('fr', 'fr'), alternate('en', 'en'), alternate('x-default', MAIN_LANGUAGE)];
}

export function usePageHead(page: () => PageHead) {
  const { locale } = useI18n();
  useHead(() => {
    const { title, description, path, indexed, translated, imageUrl, structuredData } = page();
    const fullTitle = title === SITE_NAME ? title : pageTitle(title);
    const url = canonicalUrl(path);
    return {
      htmlAttrs: { lang: locale.value },
      title: fullTitle,
      link: [{ rel: 'canonical', href: url }, ...(translated ? languageAlternates(path) : [])],
      meta: [
        { name: 'description', content: description },
        { name: 'robots', content: indexed ? 'index,follow' : 'noindex,follow' },
        { property: 'og:site_name', content: SITE_NAME },
        { property: 'og:type', content: 'website' },
        { property: 'og:title', content: fullTitle },
        { property: 'og:description', content: description },
        { property: 'og:url', content: url },
        ...(imageUrl ? [{ property: 'og:image', content: imageUrl }] : []),
      ],
      script: structuredData ? [{ type: 'application/ld+json', innerHTML: JSON.stringify(structuredData) }] : [],
    };
  });
}
