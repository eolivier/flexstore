import type { RouteLocationRaw, Router } from 'vue-router';
import { hasLanguagePrefix, localizedPath, type ShopLanguage } from '@/utils/language';

// An address that already says its language (/en/about) is the one asked for: only the others get a prefix
function localizedUnlessPrefixed(path: string, language: ShopLanguage): string {
  return hasLanguagePrefix(path) ? path : localizedPath(path, language);
}

// An address given as a path (a string, or an object with a path): the translated pages get the prefix of the
// language. The other addresses (names, relative paths) are left as they are
function localized(to: RouteLocationRaw, language: ShopLanguage): RouteLocationRaw {
  if (typeof to === 'string') {
    const [path, ...rest] = to.split(/(?=[?#])/);
    return localizedUnlessPrefixed(path, language) + rest.join('');
  }
  const path = 'path' in to ? to.path : undefined;
  if (path?.startsWith('/')) {
    return { ...to, path: localizedUnlessPrefixed(path, language) };
  }
  return to;
}

// Every link and every navigation of the application says /products or /about: the router gives them the
// address of the language of the page, so that the links of an English page lead to English pages (also in the
// HTML that is pre-rendered, which search engines follow). The language is read when the address is resolved, so
// the links follow a change of language
export function localizeRouter(router: Router, language: () => ShopLanguage) {
  const resolve = router.resolve.bind(router);
  const push = router.push.bind(router);
  const replace = router.replace.bind(router);
  router.resolve = ((to: RouteLocationRaw, currentLocation?: never) =>
    resolve(localized(to, language()), currentLocation)) as Router['resolve'];
  router.push = (to) => push(localized(to, language()));
  router.replace = (to) => replace(localized(to, language()));
}
