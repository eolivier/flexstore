import type { Product } from '@/models/product';

// Without case nor accents: "vetement" finds "Vêtement"
function normalized(text: string): string {
  return text
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase();
}

// Every word searched is in the text, in any order; an empty search matches any text
function containsEveryWord(text: string, search: string): boolean {
  const words = normalized(search).split(/\s+/).filter(Boolean);
  const normalizedText = normalized(text);
  return words.every((word) => normalizedText.includes(word));
}

// Every word searched is in the name or the description of the product
export function matchesSearch(product: Product, search: string): boolean {
  return containsEveryWord(`${product.name ?? ''} ${product.description ?? ''}`, search);
}

// The search of the catalog also looks in the name of the category, in the language of the customer: the
// products are written in one language, their categories are translated ("vetements" finds the clothes)
export interface CatalogSearch {
  text: string;
  categoryName: (category: string) => string;
}

export function matchesCatalogSearch(product: Product, catalogSearch: CatalogSearch): boolean {
  const categoryName = catalogSearch.categoryName(product.category);
  const productText = `${product.name ?? ''} ${product.description ?? ''} ${categoryName}`;
  return containsEveryWord(productText, catalogSearch.text);
}
