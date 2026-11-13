import { describe, expect, it } from 'vitest';
import { routes } from './routes';

const indexedPaths = () => routes.filter((route) => route.meta?.indexed).map((route) => route.path);

describe('routes', () => {
  it('should never list, nor pre-render, a page that needs a signed-in customer', () => {
    // Arrange
    const privateRoutes = routes.filter((route) => route.meta?.requiresAuth || route.meta?.requiresAdmin);
    // Act
    const indexedPrivateRoutes = privateRoutes.filter((route) => route.meta?.indexed);
    // Assert
    expect(privateRoutes.length).toBeGreaterThan(0);
    expect(indexedPrivateRoutes).toEqual([]);
  });

  it('should have each public page in French and in English, under the prefix of its language', () => {
    // Act
    const paths = indexedPaths();
    // Assert
    expect(paths).toEqual(
      expect.arrayContaining(['/fr', '/en', '/fr/produits', '/en/products/:id/:slug?', '/fr/a-propos', '/fr/produits/:id/:slug?'])
    );
  });

  it('should have no public page without a prefix: the addresses without prefix lead to the French pages', () => {
    // Arrange
    const unprefixedPaths = routes.map((route) => route.path).filter((path) => !path.startsWith('/fr'));
    // Act
    const indexedUnprefixedPaths = indexedPaths().filter((path) => !/^\/(fr|en)/.test(path));
    // Assert
    expect(indexedUnprefixedPaths).toEqual([]);
    expect(unprefixedPaths).toEqual(expect.arrayContaining(['/', '/about', '/products/:id/:slug?']));
  });
});
