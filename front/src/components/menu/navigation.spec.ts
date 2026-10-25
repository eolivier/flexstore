import { describe, expect, it } from 'vitest';
import { accountLinksOf } from './navigation';

describe('navigation', () => {
  it('a visitor who is not signed in has no link to an account', () => {
    // Act
    const links = accountLinksOf(false);
    // Assert
    expect(links).toEqual([]);
  });

  it('a signed-in customer has the links to their profile, orders and addresses', () => {
    // Act
    const links = accountLinksOf(true);
    // Assert
    expect(links.map((link) => link.label)).toEqual(['nav.profile', 'nav.orders', 'nav.addresses']);
  });
});
