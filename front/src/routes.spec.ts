import { describe, expect, it } from 'vitest';
import { routes } from './routes';

const indexedPaths = () => routes.filter((route) => route.meta?.indexed).map((route) => route.path);

describe('routes', () => {
  it('never lists, nor pre-renders, a page that needs a signed-in customer', () => {
    const privateRoutes = routes.filter((route) => route.meta?.requiresAuth || route.meta?.requiresAdmin);

    expect(privateRoutes.length).toBeGreaterThan(0);
    expect(privateRoutes.filter((route) => route.meta?.indexed)).toEqual([]);
  });

  it('has each public page in French and in English, under the prefix of its language', () => {
    expect(indexedPaths()).toEqual(
      expect.arrayContaining(['/fr', '/en', '/fr/produits', '/en/products/:id/:slug?', '/fr/a-propos', '/fr/produits/:id/:slug?'])
    );
  });

  it('has no public page without a prefix: the addresses without prefix lead to the French pages', () => {
    const unprefixedPaths = routes.map((route) => route.path).filter((path) => !path.startsWith('/fr'));

    expect(indexedPaths().filter((path) => !/^\/(fr|en)/.test(path))).toEqual([]);
    expect(unprefixedPaths).toEqual(expect.arrayContaining(['/', '/about', '/products/:id/:slug?']));
  });
});
