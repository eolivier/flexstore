// The languages of the shop: the pages that search engines list exist in both, each under the prefix of its
// language (/fr/about, /en/about)
export type ShopLanguage = 'fr' | 'en';

// The language of the shop for a visitor who has not chosen one, and the one of the pages without prefix
export const MAIN_LANGUAGE: ShopLanguage = 'fr';
// The languages of the pages, in the order of their hreflang
export const PAGE_LANGUAGES: ShopLanguage[] = ['fr', 'en'];

// The pages that exist in each language: the others (cart, account, administration...) follow the chosen one
const TRANSLATED_AREAS = ['/products', '/about', '/legal-notice', '/terms-of-sale'];

function isArea(path: string, area: string): boolean {
  return path === area || path.startsWith(`${area}/`);
}

function languagePrefixOf(language: ShopLanguage): string {
  return `/${language}`;
}

// An address that says its language (/en/about); the addresses without prefix (/about) do not
export function hasLanguagePrefix(path: string): boolean {
  return PAGE_LANGUAGES.some((language) => isArea(path, languagePrefixOf(language)));
}

export function languageOfPath(path: string): ShopLanguage {
  return PAGE_LANGUAGES.find((language) => isArea(path, languagePrefixOf(language))) ?? MAIN_LANGUAGE;
}

// /en/products -> /products, /en -> /
export function withoutLanguage(path: string): string {
  if (!hasLanguagePrefix(path)) return path;
  return path.slice(languagePrefixOf(languageOfPath(path)).length) || '/';
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
  const prefix = languagePrefixOf(language);
  return unprefixed === '/' ? prefix : `${prefix}${unprefixed}`;
}

export function isShopLanguage(language: string): language is ShopLanguage {
  return PAGE_LANGUAGES.some((shopLanguage) => shopLanguage === language);
}
