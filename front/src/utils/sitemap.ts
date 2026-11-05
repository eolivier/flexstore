import { absoluteUrl } from './pageAddress';
import { MAIN_LANGUAGE, type ShopLanguage } from './language';

const XML_ESCAPES: Record<string, string> = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&apos;',
};

function escapedXml(text: string): string {
  return text.replace(/[&<>"']/g, (character) => XML_ESCAPES[character]);
}

// A page of the shop, with its address in each language
export interface SitemapPage {
  paths: Record<ShopLanguage, string>;
}

const LANGUAGES: ShopLanguage[] = ['fr', 'en'];

// The sitemap of the shop (sitemaps.org): the canonical address of each page that the search engines may list,
// in each language, each one with the addresses of the page in the other languages (hreflang)
export function sitemapXml(shopUrl: string, pages: SitemapPage[]): string {
  const entries = pages.flatMap((page) => {
    const alternate = (hreflang: string, language: ShopLanguage) => {
      const href = escapedXml(absoluteUrl(shopUrl, page.paths[language]));
      return `    <xhtml:link rel="alternate" hreflang="${hreflang}" href="${href}"/>`;
    };
    const alternates = [
      ...LANGUAGES.map((language) => alternate(language, language)),
      alternate('x-default', MAIN_LANGUAGE),
    ];
    return LANGUAGES.map((language) =>
      [
        '  <url>',
        `    <loc>${escapedXml(absoluteUrl(shopUrl, page.paths[language]))}</loc>`,
        ...alternates,
        '  </url>',
      ].join('\n')
    );
  });
  return [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">',
    ...[...new Set(entries)],
    '</urlset>',
    '',
  ].join('\n');
}

// robots.txt tells where the sitemap is: its address must be absolute, so it is added when the address of the
// shop is known
export function robotsTxtWithSitemap(robotsTxt: string, shopUrl: string): string {
  return `${robotsTxt.trimEnd()}\n\nSitemap: ${shopUrl}/sitemap.xml\n`;
}
