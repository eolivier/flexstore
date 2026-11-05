// The languages of the shop: the pages that search engines list exist in both, each under the prefix of its
// language (/fr/about, /en/about)
export type ShopLanguage = 'fr' | 'en';

// The language of the shop for a visitor who has not chosen one, and the one of the pages without prefix
export const MAIN_LANGUAGE: ShopLanguage = 'fr';
// The languages of the pages, in the order of their hreflang
export const PAGE_LANGUAGES: ShopLanguage[] = ['fr', 'en'];

// The pages that exist in each language: the others (cart, account, administration...) follow the chosen one
const TRANSLATED_AREAS = ['/products', '/about', '/legal-notice', '/terms-of-sale'];

// The words of an address that change with the language, without accent (/fr/produits/vetements,
// /en/products/clothing). The addresses of the application use the English words; the others are only seen by
// the visitors and the search engines
const ADDRESS_WORDS: Record<ShopLanguage, Record<string, string>> = {
  fr: {
    products: 'produits',
    about: 'a-propos',
    'legal-notice': 'mentions-legales',
    'terms-of-sale': 'conditions-generales-de-vente',
    clothing: 'vetements',
    accessories: 'accessoires',
    electronics: 'electronique',
  },
  en: {},
};

const PRODUCTS_WORDS = ['products', ADDRESS_WORDS.fr.products];

// Only the area (/products) and the category after it (/products/clothing) are words: the id and the slug of a
// product are left as they are
function isWordPosition(words: string[], index: number): boolean {
  return index === 0 || (index === 1 && PRODUCTS_WORDS.includes(words[0]));
}

function mapWords(path: string, wordOf: (word: string) => string): string {
  const words = path.split('/').slice(1);
  return `/${words.map((word, index) => (isWordPosition(words, index) ? wordOf(word) : word)).join('/')}`;
}

function inLanguage(path: string, language: ShopLanguage): string {
  return mapWords(path, (word) => ADDRESS_WORDS[language][word] ?? word);
}

function inEnglish(path: string, language: ShopLanguage): string {
  const englishWords = Object.fromEntries(
    Object.entries(ADDRESS_WORDS[language]).map(([en, other]) => [other, en])
  );
  return mapWords(path, (word) => englishWords[word] ?? word);
}

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

// The address of the application, in English, of a page: /fr/produits/vetements -> /products/clothing,
// /en -> /
export function withoutLanguage(path: string): string {
  if (!hasLanguagePrefix(path)) return path;
  const language = languageOfPath(path);
  return inEnglish(path.slice(languagePrefixOf(language).length) || '/', language);
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
  return unprefixed === '/' ? prefix : `${prefix}${inLanguage(unprefixed, language)}`;
}

export function isShopLanguage(language: string): language is ShopLanguage {
  return PAGE_LANGUAGES.some((shopLanguage) => shopLanguage === language);
}
