import { afterEach, describe, expect, it, vi } from 'vitest';
import { canonicalUrl, pageTitle, shortDescription, siteUrl } from './seo';

describe('seo', () => {
  afterEach(() => vi.unstubAllEnvs());

  describe('siteUrl', () => {
    it('is the configured address without its final slash', () => {
      vi.stubEnv('VITE_SITE_URL', 'https://shop.example/');

      expect(siteUrl()).toBe('https://shop.example');
    });

    it('is the address of the current page when none is configured', () => {
      vi.stubEnv('VITE_SITE_URL', '');

      expect(siteUrl()).toBe(window.location.origin);
    });
  });

  describe('canonicalUrl', () => {
    it('drops the query, the fragment and the final slash of the page', () => {
      vi.stubEnv('VITE_SITE_URL', 'https://shop.example');

      expect(canonicalUrl('/products/?q=jeans#top')).toBe('https://shop.example/products');
    });

    it('keeps a slash for the home page', () => {
      vi.stubEnv('VITE_SITE_URL', 'https://shop.example');

      expect(canonicalUrl('/')).toBe('https://shop.example/');
    });
  });

  it('puts the name of the shop after the title of a page', () => {
    expect(pageTitle('Clothing')).toBe('Clothing | Flexstore');
  });

  describe('shortDescription', () => {
    it('keeps a short text on one line', () => {
      expect(shortDescription('  Soft\n  cotton  ')).toBe('Soft cotton');
    });

    it('cuts a long text at a word', () => {
      const long = 'word '.repeat(60);

      const description = shortDescription(long);

      expect(description.length).toBeLessThanOrEqual(160);
      expect(description.endsWith('word…')).toBe(true);
    });
  });
});
