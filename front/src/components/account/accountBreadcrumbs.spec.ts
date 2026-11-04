import { describe, expect, it } from 'vitest';
import { accountBreadcrumbsOf } from './accountBreadcrumbs';

describe('accountBreadcrumbs', () => {
  it('the home of the customer own space is the only step on its page', () => {
    // Act
    const breadcrumbs = accountBreadcrumbsOf('/user-profile');
    // Assert
    expect(breadcrumbs).toEqual([{ label: 'nav.profile' }]);
  });

  it('the addresses go back to the home of the customer own space', () => {
    // Act
    const breadcrumbs = accountBreadcrumbsOf('/account/addresses');
    // Assert
    expect(breadcrumbs).toEqual([
      { label: 'nav.profile', to: '/user-profile' },
      { label: 'addressBook.title' },
    ]);
  });

  it('the orders go back to the home of the customer own space', () => {
    // Act
    const breadcrumbs = accountBreadcrumbsOf('/orders');
    // Assert
    expect(breadcrumbs).toEqual([{ label: 'nav.profile', to: '/user-profile' }, { label: 'nav.orders' }]);
  });

  it.each([
    { path: '/orders/order-1', description: 'the detail of an order' },
    { path: '/orders/order-1/confirmation', description: 'the confirmation of an order' },
  ])('$description goes through the list of the orders', ({ path }) => {
    // Act
    const breadcrumbs = accountBreadcrumbsOf(path);
    // Assert
    expect(breadcrumbs).toEqual([
      { label: 'nav.profile', to: '/user-profile' },
      { label: 'nav.orders', to: '/orders' },
      { label: 'orders.detail' },
    ]);
  });

  it('a page outside the customer own space has no trail', () => {
    // Act
    const breadcrumbs = accountBreadcrumbsOf('/products/');
    // Assert
    expect(breadcrumbs).toEqual([]);
  });
});
