// src/mappers/product.mapper.ts
import type { JsonProduct } from '@/generated';
import type { Product } from '@/models/product';

export function fromApi(jsonProduct: JsonProduct): Product {
  return {
    id: jsonProduct.id,
    name: jsonProduct.name,
    description: (jsonProduct as any).description,
    category: (jsonProduct as any).category,
    price: jsonProduct.price,
    currency: (jsonProduct as any).currency ?? 'EUR'
  };
}
