import { afterEach, describe, expect, it, vi } from 'vitest';
import { canonicalUrl, pageTitle, shortDescription, siteUrl } from './seo';

describe('seo', () => {
  afterEach(() => vi.unstubAllEnvs());

  describe('siteUrl', () => {
    it('should be the configured address without its final slash', () => {
      // Arrange
      vi.stubEnv('VITE_SITE_URL', 'https://shop.example/');
      // Act
      const url = siteUrl();
      // Assert
      expect(url).toBe('https://shop.example');
    });

    it('should be the address of the current page when none is configured', () => {
      // Arrange
      vi.stubEnv('VITE_SITE_URL', '');
      // Act
      const url = siteUrl();
      // Assert
      expect(url).toBe(window.location.origin);
    });
  });

  describe('canonicalUrl', () => {
    it('should drop the query, the fragment and the final slash of the page', () => {
      // Arrange
      vi.stubEnv('VITE_SITE_URL', 'https://shop.example');
      // Act
      const url = canonicalUrl('/products/?q=jeans#top');
      // Assert
      expect(url).toBe('https://shop.example/products');
    });

    it('should keep a slash for the home page', () => {
      // Arrange
      vi.stubEnv('VITE_SITE_URL', 'https://shop.example');
      // Act
      const url = canonicalUrl('/');
      // Assert
      expect(url).toBe('https://shop.example/');
    });
  });

  it('should put the name of the shop after the title of a page', () => {
    // Act
    const title = pageTitle('Clothing');
    // Assert
    expect(title).toBe('Clothing | Flexstore');
  });

  describe('shortDescription', () => {
    it('should keep a short text on one line', () => {
      // Act
      const description = shortDescription('  Soft\n  cotton  ');
      // Assert
      expect(description).toBe('Soft cotton');
    });

    it('should cut a long text at a word', () => {
      // Arrange
      const long = 'word '.repeat(60);
      // Act
      const description = shortDescription(long);
      // Assert
      expect(description.length).toBeLessThanOrEqual(160);
      expect(description.endsWith('word…')).toBe(true);
    });
  });
});
