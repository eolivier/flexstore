import { describe, expect, it } from 'vitest';
import { FullName } from './user';

describe('FullName', () => {
  it.each([
    { person: { firstName: 'Jane', lastName: 'Doe' }, expected: 'Jane Doe' },
    { person: { firstName: 'Admin', lastName: '' }, expected: 'Admin' },
    { person: { firstName: '', lastName: 'Doe' }, expected: 'Doe' },
    { person: null, expected: '' },
  ])('the full name of $person reads "$expected"', ({ person, expected }) => {
    // Act
    const value = FullName.of(person).value;
    // Assert
    expect(value).toBe(expected);
  });

  it.each([
    { person: { firstName: 'jane', lastName: 'doe' }, expected: 'JD' },
    { person: { firstName: 'Admin', lastName: '' }, expected: 'A' },
    { person: undefined, expected: '' },
  ])('the initials of $person are "$expected"', ({ person, expected }) => {
    // Act
    const initials = FullName.of(person).initials;
    // Assert
    expect(initials).toBe(expected);
  });
});
