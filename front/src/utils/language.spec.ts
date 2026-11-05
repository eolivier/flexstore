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
  ])('%s is in %s', (path, language) => {
    // Act
    const found = languageOfPath(path);
    // Assert
    expect(found).toBe(language);
  });
});

describe('hasLanguagePrefix', () => {
  it.each(['/fr', '/en', '/fr/about', '/en/products/1/jeans'])('%s says its language', (path) => {
    // Act
    const prefixed = hasLanguagePrefix(path);
    // Assert
    expect(prefixed).toBe(true);
  });

  it.each(['/', '/about', '/products/1', '/entrance', '/french'])('%s does not say its language', (path) => {
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
    ['/fr/products/clothing', '/products/clothing'],
    ['/about', '/about'],
  ])('%s becomes %s', (path, unprefixed) => {
    // Act
    const found = withoutLanguage(path);
    // Assert
    expect(found).toBe(unprefixed);
  });
});

describe('isTranslatedPath', () => {
  it.each(['/', '/fr', '/en', '/products', '/products/1/jeans', '/fr/about', '/en/legal-notice'])(
    '%s exists in each language',
    (path) => {
      // Act
      const translated = isTranslatedPath(path);
      // Assert
      expect(translated).toBe(true);
    }
  );

  it.each(['/cart', '/login', '/admin/products', '/account/wishlist', '/orders/1', '/productsX'])(
    '%s follows the language chosen',
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
    ['/products/clothing', 'fr', '/fr/products/clothing'],
    ['/products/clothing', 'en', '/en/products/clothing'],
    ['/en/products/clothing', 'fr', '/fr/products/clothing'],
    ['/fr/about', 'en', '/en/about'],
    ['/en', 'fr', '/fr'],
  ] as const)('%s in %s is %s', (path, language, localized) => {
    // Act
    const found = localizedPath(path, language);
    // Assert
    expect(found).toBe(localized);
  });

  it('keeps the address of a page that is not translated', () => {
    // Act
    const cart = localizedPath('/cart', 'en');
    // Assert
    expect(cart).toBe('/cart');
  });
});
