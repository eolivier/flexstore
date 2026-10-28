import type { Product } from '@/models/product';
import { containsEveryWord } from './textSearch';


// Every word searched is in the SKU, the name or the description of the product
export function matchesSearch(product: Product, search: string): boolean {
  return containsEveryWord(`${product.sku} ${product.name ?? ''} ${product.description ?? ''}`, search);
}

// The search of the catalog looks in what the customer reads: the name and the description of the product,
// loaded in the language shown, and the name of its category in that language ("vetements" finds the clothes)
export interface CatalogSearch {
  text: string;
  categoryName: (category: string) => string;
}

export function matchesCatalogSearch(product: Product, catalogSearch: CatalogSearch): boolean {
  const categoryName = catalogSearch.categoryName(product.category);
  const searchedText = `${product.name ?? ''} ${product.description} ${categoryName}`;
  return containsEveryWord(searchedText, catalogSearch.text);
}
