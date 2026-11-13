import { describe, expect, it } from 'vitest';
import { emptyPromotion, isValidPromotion, promotionFormOf, promotionOf, type PromotionForm } from './promotion';

const form: PromotionForm = { percentage: '25', startsAt: '2026-11-10T09:30', endsAt: '2026-11-17T18:00' };

describe('promotion', () => {
  it('a product without promotion should be valid', () => {
    // Act
    const valid = isValidPromotion(emptyPromotion());
    // Assert
    expect(valid).toBe(true);
  });

  it.each(['0.01', '25', '100'])('a promotion of %s % over a period should be valid', (percentage) => {
    // Act
    const valid = isValidPromotion({ ...form, percentage });
    // Assert
    expect(valid).toBe(true);
  });

  it.each(['0', '-5', '100.5', '150'])('a promotion of %s % should be invalid', (percentage) => {
    // Act
    const valid = isValidPromotion({ ...form, percentage });
    // Assert
    expect(valid).toBe(false);
  });

  it.each([
    { startsAt: '', endsAt: '2026-11-17T18:00' },
    { startsAt: '2026-11-10T09:30', endsAt: '' },
    { startsAt: '2026-11-10T09:30', endsAt: '2026-11-10T09:30' },
    { startsAt: '2026-11-10T09:30', endsAt: '2026-11-10T09:29' },
  ])('a promotion from "$startsAt" to "$endsAt" should be invalid', (period) => {
    // Act
    const valid = isValidPromotion({ ...form, ...period });
    // Assert
    expect(valid).toBe(false);
  });

  it('an empty promotion should send nothing', () => {
    // Act
    const promotion = promotionOf(emptyPromotion());
    // Assert
    expect(promotion).toBeUndefined();
  });

  it('a typed promotion should send its percentage and its moments as ISO instants', () => {
    // Act
    const promotion = promotionOf(form);
    // Assert
    expect(promotion).toEqual({
      percentage: 25,
      startsAt: new Date('2026-11-10T09:30').toISOString(),
      endsAt: new Date('2026-11-17T18:00').toISOString(),
    });
  });

  it.each([null, undefined])('a product without promotion (%s) should give an empty form', (promotion) => {
    // Act
    const typed = promotionFormOf(promotion);
    // Assert
    expect(typed).toEqual(emptyPromotion());
  });

  it('a promotion read from the API should give the form it was typed in', () => {
    // Arrange
    const promotion = promotionOf(form)!;
    // Act
    const typed = promotionFormOf(promotion);
    // Assert
    expect(typed).toEqual(form);
  });
});
