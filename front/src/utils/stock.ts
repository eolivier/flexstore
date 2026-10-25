// At most this many units: a few left
export const FEW_UNITS = 5;

export type StockState = 'outOfStock' | 'fewLeft' | 'inStock';

export function stockStateOf(quantity: number): StockState {
  if (quantity === 0) return 'outOfStock';
  if (quantity <= FEW_UNITS) return 'fewLeft';
  return 'inStock';
}
