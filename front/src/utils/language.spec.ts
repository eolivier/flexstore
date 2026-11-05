import { describe, expect, it } from 'vitest';
import { isTranslatedPath, languageOfPath, localizedPath, withoutLanguage } from './language';

describe('languageOfPath', () => {
  it.each([
    ['/', 'fr'],
    ['/products/clothing', 'fr'],
    ['/en', 'en'],
    ['/en/products/clothing', 'en'],
    ['/entrance', 'fr'],
  ])('%s is in %s', (path, language) => {
    // Act
    const found = languageOfPath(path);
    // Assert
    expect(found).toBe(language);
  });
});

describe('withoutLanguage', () => {
  it.each([
    ['/en', '/'],
    ['/en/about', '/about'],
    ['/about', '/about'],
  ])('%s becomes %s', (path, unprefixed) => {
    // Act
    const found = withoutLanguage(path);
    // Assert
    expect(found).toBe(unprefixed);
  });
});

describe('isTranslatedPath', () => {
  it.each(['/', '/en', '/products', '/products', '/products/1/jeans', '/en/about', '/legal-notice'])(
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
    ['/', 'en', '/en'],
    ['/products/clothing', 'en', '/en/products/clothing'],
    ['/en/products/clothing', 'en', '/en/products/clothing'],
    ['/en/products/clothing', 'fr', '/products/clothing'],
    ['/en', 'fr', '/'],
    ['/about', 'fr', '/about'],
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
