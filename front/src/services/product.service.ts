import { isAxiosError } from "axios";
import { productsApi } from "@/api";
import type { Product } from "@/models/product";
import { fromApi } from '@/mappers/product.mapper';
import { useLoadingStore } from '@/stores/loading.store';

export async function getAllProducts(): Promise<Product[]> {
  const response = await useLoadingStore().track(productsApi.getProducts());
  return response.data.map(fromApi);
}

// Returns null when the product does not exist (404)
export async function getProduct(id: string): Promise<Product | null> {
  try {
    const response = await useLoadingStore().track(productsApi.getProduct(id));
    return fromApi(response.data);
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    throw error;
  }
}
