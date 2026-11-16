import type { Product } from '@/models/product';
import { containsEveryWord } from './textSearch';

// Every word searched is in the SKU, the name or the description of the product
export function matchesSearch(product: Product, search: string): boolean {
  return containsEveryWord(`${product.sku} ${product.name ?? ''} ${product.description ?? ''}`, search);
}
