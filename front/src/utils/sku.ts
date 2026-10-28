// The Stock Keeping Unit of a product: the reference the shop gives to it, unique in the catalog

export const SKU_MAX_LENGTH = 32;

const WELL_FORMED_SKU = /^[A-Z0-9]+(-[A-Z0-9]+)*$/;

// As the API stores it: the spaces around dropped, the letters in upper case
export function normalizedSku(typedSku: string): string {
  return typedSku.trim().toUpperCase();
}

// Upper-case letters and digits, in groups separated by single hyphens, once normalized
export function isWellFormedSku(typedSku: string): boolean {
  const sku = normalizedSku(typedSku);
  return sku.length <= SKU_MAX_LENGTH && WELL_FORMED_SKU.test(sku);
}
