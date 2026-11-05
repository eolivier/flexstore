import { describe, expect, it } from 'vitest';
import { routes } from './routes';

describe('routes', () => {
  it('never lists, nor pre-renders, a page that needs a signed-in customer', () => {
    const privateRoutes = routes.filter((route) => route.meta?.requiresAuth || route.meta?.requiresAdmin);

    expect(privateRoutes.length).toBeGreaterThan(0);
    expect(privateRoutes.filter((route) => route.meta?.indexed)).toEqual([]);
  });

  it('lists the home page, the catalog and the product pages', () => {
    const indexedPaths = routes.filter((route) => route.meta?.indexed).map((route) => route.path);

    expect(indexedPaths).toEqual(expect.arrayContaining(['/', '/products/', '/products/:id/:slug?']));
  });
});
