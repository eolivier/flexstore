import { describe, expect, it } from 'vitest';
import { fullName, initials } from './user';

describe('user', () => {
  it.each([
    { user: { firstName: 'Jane', lastName: 'Doe' }, expected: 'Jane Doe' },
    { user: { firstName: 'Admin', lastName: '' }, expected: 'Admin' },
    { user: { firstName: '', lastName: 'Doe' }, expected: 'Doe' },
    { user: null, expected: '' },
  ])('the full name of $user is "$expected"', ({ user, expected }) => {
    // Act
    const name = fullName(user);
    // Assert
    expect(name).toBe(expected);
  });

  it.each([
    { user: { firstName: 'jane', lastName: 'doe' }, expected: 'JD' },
    { user: { firstName: 'Admin', lastName: '' }, expected: 'A' },
    { user: undefined, expected: '' },
  ])('the initials of $user are "$expected"', ({ user, expected }) => {
    // Act
    const userInitials = initials(user);
    // Assert
    expect(userInitials).toBe(expected);
  });
});
