// The carrier and the tracking number of the parcel of an order, given by an administrator when the order is
// shipped (or corrected afterwards). The two are given together, or not at all

// The carriers an administrator chooses from (the back end knows the tracking page of each one but OTHER)
export const CARRIERS = ['COLISSIMO', 'CHRONOPOST', 'UPS', 'DHL', 'FEDEX', 'OTHER'];

export const MAX_TRACKING_NUMBER_LENGTH = 40;

const TRACKING_NUMBER_PATTERN = /^[A-Za-z0-9-]+$/;

export interface TrackingInput {
  carrier: string;
  trackingNumber: string;
}

export const NO_TRACKING: TrackingInput = { carrier: '', trackingNumber: '' };

export function hasTracking(tracking: TrackingInput): boolean {
  return tracking.carrier !== '' && tracking.trackingNumber.trim() !== '';
}

// Letters, digits and hyphens, without spaces, 40 characters at most
export function isValidTrackingNumber(typedNumber: string): boolean {
  const trackingNumber = typedNumber.trim();
  return (
    trackingNumber.length > 0 &&
    trackingNumber.length <= MAX_TRACKING_NUMBER_LENGTH &&
    TRACKING_NUMBER_PATTERN.test(trackingNumber)
  );
}

// A carrier chosen, and a number that is valid
export function isCompleteTracking(tracking: TrackingInput): boolean {
  return CARRIERS.includes(tracking.carrier) && isValidTrackingNumber(tracking.trackingNumber);
}

// The next status of a processed order is the one that gives its tracking
export function isShippedNext(status: string): boolean {
  return status === 'PROCESSING';
}

// Shipped, or delivered: the tracking may still be corrected
export function allowsTrackingCorrection(status: string): boolean {
  return status === 'SHIPPED' || status === 'DELIVERED';
}

// What is sent to the back end: the number without the spaces around it
export function trackingToSend(tracking: TrackingInput): TrackingInput {
  return { carrier: tracking.carrier, trackingNumber: tracking.trackingNumber.trim() };
}
