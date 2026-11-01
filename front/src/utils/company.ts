// The SIREN and the intra-community VAT number as typed: the spaces are dropped, the letters put in upper case
export function normalizedIdentifier(typed: string): string {
  return typed.replace(/\s/g, '').toUpperCase();
}

// The SIREN of a company: 9 digits
export function isValidSiren(siren: string): boolean {
  return /^[0-9]{9}$/.test(normalizedIdentifier(siren));
}

// The intra-community VAT number, optional: its country (FR), then 2 to 13 digits or letters; a French one
// is FR, a key of 2 characters, then the SIREN
export function isValidVatNumber(vatNumber: string, siren: string): boolean {
  const vat = normalizedIdentifier(vatNumber);
  if (vat === '') return true;
  if (!/^[A-Z]{2}[0-9A-Z]{2,13}$/.test(vat)) return false;
  return !vat.startsWith('FR') || vat.endsWith(normalizedIdentifier(siren));
}
