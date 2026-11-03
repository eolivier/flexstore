import { describe, expect, it } from 'vitest';
import {
  allowsTrackingCorrection,
  hasTracking,
  isCompleteTracking,
  isShippedNext,
  isValidTrackingNumber,
  NO_TRACKING,
  trackingToSend,
} from './tracking';

describe('tracking', () => {
  it.each(['6A12345678901', '1Z999AA10123456784', 'AB-123-CD', '7', '  6A123  '])(
    'the tracking number "%s" is valid',
    (typedNumber) => {
      // Act
      const valid = isValidTrackingNumber(typedNumber);
      // Assert
      expect(valid).toBe(true);
    }
  );

  it.each(['', '   ', 'AB 123', 'AB_123', 'AB#123', 'é1234'])(
    'the tracking number "%s" is invalid',
    (typedNumber) => {
      // Act
      const valid = isValidTrackingNumber(typedNumber);
      // Assert
      expect(valid).toBe(false);
    }
  );

  it.each([
    { length: 40, valid: true },
    { length: 41, valid: false },
  ])('a tracking number of $length characters is valid: $valid', ({ length, valid }) => {
    // Act
    const isValid = isValidTrackingNumber('A'.repeat(length));
    // Assert
    expect(isValid).toBe(valid);
  });

  it.each([
    { carrier: 'UPS', trackingNumber: '1Z999', complete: true },
    { carrier: '', trackingNumber: '1Z999', complete: false },
    { carrier: 'UPS', trackingNumber: '', complete: false },
    { carrier: 'PIGEON', trackingNumber: '1Z999', complete: false },
    { carrier: 'UPS', trackingNumber: '1Z 999', complete: false },
  ])('the tracking $carrier / "$trackingNumber" is complete: $complete', (tracking) => {
    // Act
    const complete = isCompleteTracking(tracking);
    // Assert
    expect(complete).toBe(tracking.complete);
  });

  it('no tracking is given by the empty carrier and number', () => {
    // Act
    const given = hasTracking(NO_TRACKING);
    // Assert
    expect(given).toBe(false);
  });

  it('a tracking is sent with its number trimmed', () => {
    // Act
    const sent = trackingToSend({ carrier: 'UPS', trackingNumber: ' 1Z999 ' });
    // Assert
    expect(sent).toEqual({ carrier: 'UPS', trackingNumber: '1Z999' });
  });

  it.each([
    { status: 'PROCESSING', shippedNext: true },
    { status: 'PAID', shippedNext: false },
    { status: 'SHIPPED', shippedNext: false },
  ])('the status $status gives the tracking next: $shippedNext', ({ status, shippedNext }) => {
    // Act
    const next = isShippedNext(status);
    // Assert
    expect(next).toBe(shippedNext);
  });

  it.each([
    { status: 'SHIPPED', allowed: true },
    { status: 'DELIVERED', allowed: true },
    { status: 'PROCESSING', allowed: false },
    { status: 'REFUNDED', allowed: false },
    { status: 'CANCELLED', allowed: false },
  ])('the tracking of an order $status may be corrected: $allowed', ({ status, allowed }) => {
    // Act
    const correctable = allowsTrackingCorrection(status);
    // Assert
    expect(correctable).toBe(allowed);
  });
});
