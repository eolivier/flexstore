import { describe, expect, it } from 'vitest';
import { robotsTxtWithSitemap, sitemapXml } from './sitemap';

describe('sitemapXml', () => {
  it('lists the canonical address of each page once', () => {
    // Act
    const sitemap = sitemapXml('https://shop.example', ['/', '/products/', '/products', '/about']);
    // Assert
    expect(sitemap).toBe(
      [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
        '  <url><loc>https://shop.example/</loc></url>',
        '  <url><loc>https://shop.example/products</loc></url>',
        '  <url><loc>https://shop.example/about</loc></url>',
        '</urlset>',
        '',
      ].join('\n')
    );
  });

  it('escapes the characters that XML reserves', () => {
    // Act
    const sitemap = sitemapXml('https://shop.example', ['/products/1/salt&pepper']);
    // Assert
    expect(sitemap).toContain('<loc>https://shop.example/products/1/salt&amp;pepper</loc>');
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
