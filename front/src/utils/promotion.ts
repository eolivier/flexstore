import type { Promotion } from '@/models/product';

// What an administrator types for the promotion of a product: a percentage and the moments it starts and ends
// (as the datetime-local fields give them, in the time zone of the browser, to the minute). An empty percentage
// means that the product has no promotion
export interface PromotionForm {
  percentage: string;
  startsAt: string;
  endsAt: string;
}

const WHOLE_PERCENTAGE = 100;
const LOCAL_DATE_TIME_LENGTH = 'YYYY-MM-DDTHH:mm'.length;
const MINUTE_IN_MILLISECONDS = 60_000;

export function emptyPromotion(): PromotionForm {
  return { percentage: '', startsAt: '', endsAt: '' };
}

export function hasPromotion(form: PromotionForm): boolean {
  return form.percentage !== '' && form.percentage !== null;
}

export function isValidPercentage(typedPercentage: string): boolean {
  const percentage = parseFloat(typedPercentage);
  return percentage > 0 && percentage <= WHOLE_PERCENTAGE;
}

// The promotion ends after it starts
export function isValidPeriod(form: PromotionForm): boolean {
  return !!form.startsAt && !!form.endsAt && new Date(form.endsAt) > new Date(form.startsAt);
}

export function isValidPromotion(form: PromotionForm): boolean {
  if (!hasPromotion(form)) return true;
  return isValidPercentage(form.percentage) && isValidPeriod(form);
}

// The promotion to send to the API; nothing when the product is not on promotion
export function promotionOf(form: PromotionForm): Promotion | undefined {
  if (!hasPromotion(form)) return undefined;
  const startsAt = new Date(form.startsAt).toISOString();
  const endsAt = new Date(form.endsAt).toISOString();
  return { percentage: parseFloat(form.percentage), startsAt, endsAt };
}

// The promotion of a product as typed in its form: the moments in the time zone of the browser
export function promotionFormOf(promotion: Promotion | null | undefined): PromotionForm {
  if (!promotion) return emptyPromotion();
  return {
    percentage: String(promotion.percentage),
    startsAt: localDateTimeOf(promotion.startsAt),
    endsAt: localDateTimeOf(promotion.endsAt),
  };
}

// An ISO instant as a datetime-local value: 2026-11-10T09:30
function localDateTimeOf(isoInstant: string): string {
  const instant = new Date(isoInstant);
  const localInstant = new Date(instant.getTime() - instant.getTimezoneOffset() * MINUTE_IN_MILLISECONDS);
  return localInstant.toISOString().slice(0, LOCAL_DATE_TIME_LENGTH);
}
