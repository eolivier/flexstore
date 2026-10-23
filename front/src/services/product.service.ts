import { isAxiosError } from "axios";
import { productsApi } from "@/api";
import type { Product } from "@/models/product";
import { fromApi } from '@/mappers/product.mapper';

export async function getAllProducts(): Promise<Product[]> {
  const res = await productsApi.getProducts();
  return res.data.map(fromApi);
}

// Returns null when the product does not exist (404)
export async function getProduct(id: string): Promise<Product | null> {
  try {
    const res = await productsApi.getProduct(id);
    return fromApi(res.data);
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    throw error;
  }
}
