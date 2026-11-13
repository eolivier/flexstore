import { describe, expect, it } from 'vitest';
import { hasLanguagePrefix, isTranslatedPath, languageOfPath, localizedPath, withoutLanguage } from './language';

describe('languageOfPath', () => {
  it.each([
    ['/fr', 'fr'],
    ['/fr/products/clothing', 'fr'],
    ['/en', 'en'],
    ['/en/products/clothing', 'en'],
    ['/about', 'fr'],
    ['/entrance', 'fr'],
  ])('%s should be in %s', (path, language) => {
    // Act
    const found = languageOfPath(path);
    // Assert
    expect(found).toBe(language);
  });
});

describe('hasLanguagePrefix', () => {
  it.each(['/fr', '/en', '/fr/about', '/en/products/1/jeans'])('%s should say its language', (path) => {
    // Act
    const prefixed = hasLanguagePrefix(path);
    // Assert
    expect(prefixed).toBe(true);
  });

  it.each(['/', '/about', '/products/1', '/entrance', '/french'])('%s should not say its language', (path) => {
    // Act
    const prefixed = hasLanguagePrefix(path);
    // Assert
    expect(prefixed).toBe(false);
  });
});

describe('withoutLanguage', () => {
  it.each([
    ['/en', '/'],
    ['/fr', '/'],
    ['/en/about', '/about'],
    ['/fr/produits/vetements', '/products/clothing'],
    ['/fr/produits/12/t-shirt-vetements', '/products/12/t-shirt-vetements'],
    ['/fr/a-propos', '/about'],
    ['/about', '/about'],
  ])('%s should become %s', (path, unprefixed) => {
    // Act
    const found = withoutLanguage(path);
    // Assert
    expect(found).toBe(unprefixed);
  });
});

describe('isTranslatedPath', () => {
  it.each(['/', '/fr', '/en', '/products', '/products/1/jeans', '/fr/about', '/en/legal-notice'])(
    '%s should exist in each language',
    (path) => {
      // Act
      const translated = isTranslatedPath(path);
      // Assert
      expect(translated).toBe(true);
    }
  );

  it.each(['/cart', '/login', '/admin/products', '/account/wishlist', '/orders/1', '/productsX'])(
    '%s should follow the language chosen',
    (path) => {
      // Act
      const translated = isTranslatedPath(path);
      // Assert
      expect(translated).toBe(false);
    }
  );
});

describe('localizedPath', () => {
  it.each([
    ['/', 'fr', '/fr'],
    ['/', 'en', '/en'],
    ['/products/clothing', 'fr', '/fr/produits/vetements'],
    ['/products/electronics', 'fr', '/fr/produits/electronique'],
    ['/products/accessories', 'fr', '/fr/produits/accessoires'],
    ['/products/clothing', 'en', '/en/products/clothing'],
    ['/products/12/jeans-slim', 'fr', '/fr/produits/12/jeans-slim'],
    ['/en/products/clothing', 'fr', '/fr/produits/vetements'],
    ['/fr/a-propos', 'en', '/en/about'],
    ['/about', 'fr', '/fr/a-propos'],
    ['/legal-notice', 'fr', '/fr/mentions-legales'],
    ['/terms-of-sale', 'fr', '/fr/conditions-generales-de-vente'],
    ['/privacy-policy', 'fr', '/fr/politique-de-confidentialite'],
    ['/fr/politique-de-confidentialite', 'en', '/en/privacy-policy'],
    ['/en', 'fr', '/fr'],
  ] as const)('%s in %s should be %s', (path, language, localized) => {
    // Act
    const found = localizedPath(path, language);
    // Assert
    expect(found).toBe(localized);
  });

  it('should keep the address of a page that is not translated', () => {
    // Act
    const cart = localizedPath('/cart', 'en');
    // Assert
    expect(cart).toBe('/cart');
  });
});
