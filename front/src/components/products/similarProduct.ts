import type { Product } from '@/models/product';
import type { Rating } from '@/stores/reviews.store';

// A product of the same category, with its stock (null while unknown) and its rating (null when nobody reviewed it)
export interface SimilarProduct {
  product: Product;
  stock: number | null;
  rating: Rating | null;
}
