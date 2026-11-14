import { afterEach, describe, expect, it, vi } from 'vitest';
import { openExternalPage } from './navigation';

describe('navigation', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('should send the browser to the external page', () => {
    // Arrange
    const assign = vi.fn();
    vi.stubGlobal('location', { assign });
    // Act
    openExternalPage('https://checkout.stripe.com/c/pay/cs_1');
    // Assert
    expect(assign).toHaveBeenCalledWith('https://checkout.stripe.com/c/pay/cs_1');
  });
});
