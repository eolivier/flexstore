import type { Product } from '@/models/product';

// Without case nor accents: "vetement" finds "Vêtement"
function normalized(text: string): string {
  return text
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase();
}

// Every word searched is in the name or the description of the product, in any order; an empty search matches
// every product
export function matchesSearch(product: Product, search: string): boolean {
  const words = normalized(search).split(/\s+/).filter(Boolean);
  const productText = normalized(`${product.name ?? ''} ${product.description ?? ''}`);
  return words.every((word) => productText.includes(word));
}
