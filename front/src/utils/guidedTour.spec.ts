import { describe, expect, it } from 'vitest';
import { guidedTourOfAccount, guidedTourSections } from './guidedTour';

describe('guidedTour', () => {
  it('should go through the sections of the shop in order', () => {
    // Act
    const messageKeys = guidedTourSections.map((section) => section.messageKey);
    // Assert
    expect(messageKeys).toEqual([
      'guidedTour.sections.catalog',
      'guidedTour.sections.cart',
      'guidedTour.sections.orders',
      'guidedTour.sections.wishlist',
      'guidedTour.sections.loyalty',
      'guidedTour.sections.preferences',
    ]);
  });

  it('should give a different icon to every section', () => {
    // Act
    const icons = guidedTourSections.map((section) => section.icon);
    // Assert
    expect(icons.every((icon) => icon.startsWith('mdi-'))).toBe(true);
    expect(new Set(icons).size).toBe(icons.length);
  });

  it.each([
    { accountChoice: 'SHOWN', expected: 'shown' },
    { accountChoice: 'HIDDEN', expected: 'hidden' },
    { accountChoice: 'NONE', expected: 'none' },
  ])('the choice $accountChoice of the account should be $expected', ({ accountChoice, expected }) => {
    // Act
    const guidedTour = guidedTourOfAccount[accountChoice];
    // Assert
    expect(guidedTour).toBe(expected);
  });

  it('should know no guided tour for a choice that the API does not define', () => {
    // Act
    const guidedTour = guidedTourOfAccount['UNKNOWN'];
    // Assert
    expect(guidedTour).toBeUndefined();
  });
});
