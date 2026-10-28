import { describe, expect, it } from 'vitest';
import { adminBreadcrumbsOf } from './adminBreadcrumbs';

describe('adminBreadcrumbs', () => {
  it('the home of the administration is the only step on its page', () => {
    // Act
    const breadcrumbs = adminBreadcrumbsOf('/admin');
    // Assert
    expect(breadcrumbs).toEqual([{ label: 'nav.admin' }]);
  });

  it('a list goes back to the home of the administration', () => {
    // Act
    const breadcrumbs = adminBreadcrumbsOf('/admin/orders');
    // Assert
    expect(breadcrumbs).toEqual([{ label: 'nav.admin', to: '/admin' }, { label: 'nav.adminOrders' }]);
  });

  it('the detail of an order goes through the list of the orders', () => {
    // Act
    const breadcrumbs = adminBreadcrumbsOf('/admin/orders/46555c9f');
    // Assert
    expect(breadcrumbs).toEqual([
      { label: 'nav.admin', to: '/admin' },
      { label: 'nav.adminOrders', to: '/admin/orders' },
      { label: 'adminOrders.detail' },
    ]);
  });

  it.each([
    { path: '/admin/products/p-1/edit', label: 'editProduct.title' },
    { path: '/admin/create-product', label: 'nav.createProduct' },
  ])('the page $path goes through the list of the products', ({ path, label }) => {
    // Act
    const breadcrumbs = adminBreadcrumbsOf(path);
    // Assert
    expect(breadcrumbs).toEqual([
      { label: 'nav.admin', to: '/admin' },
      { label: 'nav.products', to: '/admin/products' },
      { label },
    ]);
  });

  it.each([
    { path: '/admin/users/u-1/edit', label: 'editUser.title' },
    { path: '/admin/create-user', label: 'nav.createUser' },
  ])('the page $path goes through the list of the users', ({ path, label }) => {
    // Act
    const breadcrumbs = adminBreadcrumbsOf(path);
    // Assert
    expect(breadcrumbs).toEqual([
      { label: 'nav.admin', to: '/admin' },
      { label: 'nav.users', to: '/admin/users' },
      { label },
    ]);
  });

  it('a page outside the administration has no trail', () => {
    // Act
    const breadcrumbs = adminBreadcrumbsOf('/products/');
    // Assert
    expect(breadcrumbs).toEqual([]);
  });
});
