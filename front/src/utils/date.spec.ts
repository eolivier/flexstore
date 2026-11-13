import { describe, expect, it } from 'vitest';
import { formatDate, formatDateTime, formatShortDay } from '@/utils/date';

describe('date formats', () => {
  it('should write a day out in the locale', () => {
    // Act
    const date = formatDate('2026-11-13T10:15:00Z', 'en');
    // Assert
    expect(date).toBe('November 13, 2026');
  });

  it('should write a day out in French', () => {
    // Act
    const date = formatDate('2026-11-13T10:15:00Z', 'fr');
    // Assert
    expect(date).toBe('13 novembre 2026');
  });

  it('should add the time to a moment', () => {
    // Act
    const dateTime = formatDateTime('2026-11-13T10:15:00', 'en');
    // Assert
    expect(dateTime).toContain('November 13, 2026');
    expect(dateTime).toContain('10:15');
  });

  it('should shorten a day of the shop for a chart', () => {
    // Act
    const label = formatShortDay('2026-11-13', 'en');
    // Assert
    expect(label).toBe('Nov 13');
  });
});
