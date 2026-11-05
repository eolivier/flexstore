import { describe, expect, it } from 'vitest';
import { robotsTxtWithSitemap, sitemapXml } from './sitemap';

const about = { paths: { fr: '/about', en: '/en/about' } };

describe('sitemapXml', () => {
  it('lists the page in each language, each one with the addresses of both (hreflang)', () => {
    // Act
    const sitemap = sitemapXml('https://shop.example', [about]);
    // Assert
    const alternates = [
      '    <xhtml:link rel="alternate" hreflang="fr" href="https://shop.example/about"/>',
      '    <xhtml:link rel="alternate" hreflang="en" href="https://shop.example/en/about"/>',
      '    <xhtml:link rel="alternate" hreflang="x-default" href="https://shop.example/about"/>',
    ];
    expect(sitemap).toBe(
      [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">',
        '  <url>',
        '    <loc>https://shop.example/about</loc>',
        ...alternates,
        '  </url>',
        '  <url>',
        '    <loc>https://shop.example/en/about</loc>',
        ...alternates,
        '  </url>',
        '</urlset>',
        '',
      ].join('\n')
    );
  });

  it('gives the canonical address of a page, without the final slash', () => {
    // Act
    const sitemap = sitemapXml('https://shop.example', [{ paths: { fr: '/', en: '/en' } }]);
    // Assert
    expect(sitemap).toContain('<loc>https://shop.example/</loc>');
    expect(sitemap).toContain('<loc>https://shop.example/en</loc>');
  });

  it('lists a page once', () => {
    // Act
    const sitemap = sitemapXml('https://shop.example', [about, about]);
    // Assert
    expect(sitemap.match(/<loc>/g)).toHaveLength(2);
  });

  it('escapes the characters that XML reserves', () => {
    // Act
    const sitemap = sitemapXml('https://shop.example', [
      { paths: { fr: '/products/1/sel&poivre', en: '/en/products/1/salt&pepper' } },
    ]);
    // Assert
    expect(sitemap).toContain('<loc>https://shop.example/products/1/sel&amp;poivre</loc>');
    expect(sitemap).toContain('href="https://shop.example/en/products/1/salt&amp;pepper"');
  });
});

describe('robotsTxtWithSitemap', () => {
  it('ends robots.txt with the address of the sitemap', () => {
    // Act
    const robotsTxt = robotsTxtWithSitemap('User-agent: *\nDisallow: /admin\n', 'https://shop.example');
    // Assert
    expect(robotsTxt).toBe('User-agent: *\nDisallow: /admin\n\nSitemap: https://shop.example/sitemap.xml\n');
  });
});
