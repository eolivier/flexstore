import { describe, expect, it } from 'vitest';
import { isEmail } from './email';

describe('isEmail', () => {
  it.each([
    { email: 'contact@flexstore.fr', expected: true },
    { email: ' contact@flexstore.fr ', expected: true },
    { email: 'contact.flexstore.fr', expected: false },
    { email: 'contact@flexstore', expected: false },
    { email: '', expected: false },
  ])('"$email" should be $expected', ({ email, expected }) => {
    // Act
    const accepted = isEmail(email);

    // Assert
    expect(accepted).toBe(expected);
  });
});
