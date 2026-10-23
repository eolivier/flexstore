// Formats an amount with 2 decimals in the current locale, followed by the currency symbol sent by the API
export function formatPrice(amount: number | undefined, currency: string | undefined, locale: string): string {
  const value = (amount ?? 0).toLocaleString(locale, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return currency ? `${value} ${currency}` : value;
}
