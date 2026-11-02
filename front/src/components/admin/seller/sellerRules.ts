// The rules of the shop on the seller, checked again by the back end

// 14 digits, maybe grouped by spaces
export function isSiret(siret: string) {
  return /^[0-9]{14}$/.test(siret.replace(/ /g, ''));
}

// The country (FR), then 2 to 13 digits or letters, maybe with spaces
export function isVatNumber(vatNumber: string) {
  return /^[A-Z]{2}[0-9A-Z]{2,13}$/.test(vatNumber.replace(/ /g, '').toUpperCase());
}

// Digits, maybe after a +, grouped by spaces, dots, dashes or parentheses
export function isPhone(phone: string) {
  return /^\+?[0-9][0-9 .()-]{5,19}$/.test(phone.trim());
}

export function isEmail(email: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
}

// With or without its scheme: www.flexstore.fr, https://www.flexstore.fr
export function isWebsite(website: string) {
  return /^(https?:\/\/)?[^\s/.]+(\.[^\s/.]+)+(\/\S*)?$/.test(website.trim());
}
