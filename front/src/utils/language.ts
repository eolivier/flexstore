// The languages of the shop. French is the one of the addresses without prefix (/about), English has the prefix
// /en (/en/about): the pages that search engines list exist in both
export type ShopLanguage = 'fr' | 'en';

export const MAIN_LANGUAGE: ShopLanguage = 'fr';
const ENGLISH_PREFIX = '/en';

// The pages that exist in each language: the others (cart, account, administration...) follow the chosen one
const TRANSLATED_AREAS = ['/products', '/about', '/legal-notice', '/terms-of-sale'];

function isArea(path: string, area: string): boolean {
  return path === area || path.startsWith(`${area}/`);
}

export function languageOfPath(path: string): ShopLanguage {
  return isArea(path, ENGLISH_PREFIX) ? 'en' : MAIN_LANGUAGE;
}

// /en/products -> /products, /en -> /
export function withoutLanguage(path: string): string {
  return isArea(path, ENGLISH_PREFIX) ? path.slice(ENGLISH_PREFIX.length) || '/' : path;
}

// A page that exists in each language: the home page, the catalog, the products, about, the legal pages
export function isTranslatedPath(path: string): boolean {
  const unprefixed = withoutLanguage(path);
  return unprefixed === '/' || TRANSLATED_AREAS.some((area) => isArea(unprefixed, area));
}

// The address of a page in a language; a page that is not translated keeps its address
export function localizedPath(path: string, language: ShopLanguage): string {
  if (!isTranslatedPath(path)) return path;
  const unprefixed = withoutLanguage(path);
  if (language === MAIN_LANGUAGE) return unprefixed;
  return unprefixed === '/' ? ENGLISH_PREFIX : `${ENGLISH_PREFIX}${unprefixed}`;
}

export function isShopLanguage(language: string): language is ShopLanguage {
  return language === 'fr' || language === 'en';
}
