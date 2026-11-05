export const SITE_NAME = 'Flexstore';

// The longest description that the search engines show in full
const MAX_DESCRIPTION_LENGTH = 160;

// The address of the shop, without the final slash: VITE_SITE_URL, or the one of the current page. Without
// either (a build with no VITE_SITE_URL), the addresses stay relative to the page, which search engines accept
export function siteUrl(): string {
  const currentOrigin = typeof window === 'undefined' ? '' : window.location.origin;
  const configured: string = import.meta.env.VITE_SITE_URL || currentOrigin;
  return configured.replace(/\/+$/, '');
}

// The one address of a page for the search engines: no query, no fragment, no final slash
// (/products/?q=jeans -> https://shop.example/products)
export function canonicalUrl(path: string): string {
  const withoutQuery = path.split(/[?#]/)[0];
  const withoutFinalSlash = withoutQuery.replace(/\/+$/, '');
  return siteUrl() + (withoutFinalSlash || '/');
}

export function pageTitle(title: string): string {
  return `${title} | ${SITE_NAME}`;
}

// A text short enough to be shown in full as the description of a page, cut at a word
export function shortDescription(text: string): string {
  const oneLine = text.replace(/\s+/g, ' ').trim();
  if (oneLine.length <= MAX_DESCRIPTION_LENGTH) return oneLine;
  const cut = oneLine.slice(0, MAX_DESCRIPTION_LENGTH - 1);
  return `${cut.slice(0, cut.lastIndexOf(' '))}…`;
}
