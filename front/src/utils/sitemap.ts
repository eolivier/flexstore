import { absoluteUrl } from './pageAddress';

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

// The sitemap of the shop (sitemaps.org): the canonical address of each page that the search engines may list
export function sitemapXml(shopUrl: string, paths: string[]): string {
  const addresses = paths.map((path) => absoluteUrl(shopUrl, path));
  const entries = [...new Set(addresses)].map((address) => `  <url><loc>${escapedXml(address)}</loc></url>`);
  return [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
    ...entries,
    '</urlset>',
    '',
  ].join('\n');
}

// robots.txt tells where the sitemap is: its address must be absolute, so it is added when the address of the
// shop is known
export function robotsTxtWithSitemap(robotsTxt: string, shopUrl: string): string {
  return `${robotsTxt.trimEnd()}\n\nSitemap: ${shopUrl}/sitemap.xml\n`;
}
