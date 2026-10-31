// The rules of the shop on the seller, checked again by the back end

// 14 digits, maybe grouped by spaces
export function isSiret(siret: string) {
  return /^[0-9]{14}$/.test(siret.replace(/ /g, ''));
}

// The country (FR), then 2 to 13 digits or letters, maybe with spaces
export function isVatNumber(vatNumber: string) {
  return /^[A-Z]{2}[0-9A-Z]{2,13}$/.test(vatNumber.replace(/ /g, '').toUpperCase());
}
