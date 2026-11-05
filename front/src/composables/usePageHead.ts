import { useHead } from '@unhead/vue';
import { canonicalUrl, pageTitle, SITE_NAME } from '@/utils/seo';

// What a page tells the search engines and the social networks about itself
export interface PageHead {
  title: string;
  description: string;
  path: string;
  indexed: boolean;
  imageUrl: string | null;
  structuredData: object | null;
}

// The head of the page follows the values the getter reads; the page that registers it last wins
export function usePageHead(page: () => PageHead) {
  useHead(() => {
    const { title, description, path, indexed, imageUrl, structuredData } = page();
    const fullTitle = title === SITE_NAME ? title : pageTitle(title);
    const url = canonicalUrl(path);
    return {
      title: fullTitle,
      link: [{ rel: 'canonical', href: url }],
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
