import { productsApi } from "@/api";
import type { Product } from "@/models/product";
import { fromApi } from '@/mappers/product.mapper';

export async function getAllProducts(): Promise<Product[]> {
  const res = await productsApi.getProducts();
  return res.data.map(fromApi);
}