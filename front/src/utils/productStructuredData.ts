import type { Product } from '@/models/product';
import type { Rating } from '@/stores/reviews.store';

// What the page of a product knows besides the product: its address, image, availability and rating
export interface ProductPage {
  product: Product;
  url: string;
  imageUrl: string | null;
  inStock: boolean | null;
  rating: Rating | null;
}

// The product as schema.org describes it (JSON-LD), for the rich results of the search engines
export function productStructuredData(page: ProductPage): object {
  const { product, url, imageUrl, inStock, rating } = page;
  return {
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: product.name,
    description: product.description,
    sku: product.sku,
    url,
    ...(imageUrl ? { image: imageUrl } : {}),
    offers: {
      '@type': 'Offer',
      url,
      price: product.currentPrice ?? product.price,
      priceCurrency: product.currency,
      ...(inStock === null ? {} : { availability: `https://schema.org/${inStock ? 'InStock' : 'OutOfStock'}` }),
    },
    ...(rating
      ? {
          aggregateRating: {
            '@type': 'AggregateRating',
            ratingValue: rating.average,
            reviewCount: rating.reviewCount,
          },
        }
      : {}),
  };
}
