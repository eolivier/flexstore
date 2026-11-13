import { afterEach, describe, expect, it, vi } from 'vitest';
import { logFailure } from '@/utils/logFailure';

describe('logFailure', () => {
  afterEach(() => vi.restoreAllMocks());

  it('should log what was attempted, then its cause', () => {
    // Arrange
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    const cause = new Error('network down');
    // Act
    logFailure('save the cart item', cause);
    // Assert
    expect(consoleError).toHaveBeenCalledWith('Failed to save the cart item', cause);
  });

  it('should log only what was attempted when there is no cause', () => {
    // Arrange
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    // Act
    logFailure('load the cart items');
    // Assert
    expect(consoleError).toHaveBeenCalledWith('Failed to load the cart items');
  });
});
