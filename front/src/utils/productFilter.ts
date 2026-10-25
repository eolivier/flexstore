import type { Product } from '@/models/product';
import { matchesSearch } from './productSearch';
import { stockStateOf, type StockState } from './stock';

// What the administrator looks for: words in the name or the description (as in the search of the catalog), a
// category, a state of the stock (null: any)
export interface ProductFilter {
  text: string;
  category: string | null;
  stockState: StockState | null;
}

export const noFilter: ProductFilter = { text: '', category: null, stockState: null };

// stock: the units of the product, null while unknown (then any state of the stock matches)
export function matchesFilter(product: Product, stock: number | null, filter: ProductFilter): boolean {
  const matchesText = matchesSearch(product, filter.text);
  const matchesCategory = !filter.category || product.category === filter.category;
  const matchesStock = !filter.stockState || (stock !== null && stockStateOf(stock) === filter.stockState);
  return matchesText && matchesCategory && matchesStock;
}
