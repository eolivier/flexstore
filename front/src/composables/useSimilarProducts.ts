import { computed, type Ref } from 'vue';
import type { Product } from '@/models/product';
import type { SimilarProduct } from '@/components/products/similarProduct';
import { useProductsStore } from '@/stores/products.store';
import { useReviewsStore } from '@/stores/reviews.store';
import { useStocksStore } from '@/stores/stocks.store';

// The other products of the category of a product that can be ordered (the products out of stock are left out),
// each with its stock and its rating
export function useSimilarProducts(product: Ref<Product | null>) {
  const productsStore = useProductsStore();
  const stocksStore = useStocksStore();
  const reviewsStore = useReviewsStore();

  return computed<SimilarProduct[]>(() =>
    productsStore.products
      .filter((p) => p.category === product.value?.category && p.id !== product.value?.id)
      .filter((p) => stocksStore.quantityOf(p.id) !== 0)
      .slice(0, 4)
      .map((p) => ({ product: p, stock: stocksStore.quantityOf(p.id), rating: reviewsStore.ratingOf(p.id) }))
  );
}
