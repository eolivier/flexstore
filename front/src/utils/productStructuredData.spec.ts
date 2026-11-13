import { describe, expect, it } from 'vitest';
import { productStructuredData, type ProductPage } from './productStructuredData';

const page = (overrides: Partial<ProductPage> = {}): ProductPage => ({
  product: {
    id: 'jeans',
    sku: 'JEANS-M',
    name: 'Slim Jeans',
    description: 'Sturdy denim',
    category: 'CLOTHING',
    price: 50,
    currentPrice: 40,
    currency: 'EUR',
  },
  url: 'https://shop.example/products/jeans',
  imageUrl: 'https://api.example/api/products/jeans/image',
  inStock: true,
  rating: { average: 4.5, reviewCount: 2 },
  ...overrides,
});

describe('productStructuredData', () => {
  it('should describe the product with the price to pay, its availability and its rating', () => {
    // Act
    const data = productStructuredData(page());
    // Assert
    expect(data).toEqual({
      '@context': 'https://schema.org',
      '@type': 'Product',
      name: 'Slim Jeans',
      description: 'Sturdy denim',
      sku: 'JEANS-M',
      url: 'https://shop.example/products/jeans',
      image: 'https://api.example/api/products/jeans/image',
      offers: {
        '@type': 'Offer',
        url: 'https://shop.example/products/jeans',
        price: 40,
        priceCurrency: 'EUR',
        availability: 'https://schema.org/InStock',
      },
      aggregateRating: { '@type': 'AggregateRating', ratingValue: 4.5, reviewCount: 2 },
    });
  });

  it('should say out of stock for a product that has no unit left', () => {
    // Act
    const data = productStructuredData(page({ inStock: false })) as { offers: { availability: string } };
    // Assert
    expect(data.offers.availability).toBe('https://schema.org/OutOfStock');
  });

  it('should leave out what is unknown: the image, the availability and the rating', () => {
    // Act
    const data = productStructuredData(page({ imageUrl: null, inStock: null, rating: null }));
    // Assert
    expect(data).not.toHaveProperty('image');
    expect(data).not.toHaveProperty('aggregateRating');
    expect(data).not.toHaveProperty('offers.availability');
  });
});
