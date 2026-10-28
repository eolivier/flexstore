import { isAxiosError } from 'axios';
import { productsApi } from '@/api';
import type { Product } from '@/models/product';
import { fromApi } from '@/mappers/product.mapper';
import { useLoadingStore } from '@/stores/loading.store';

// language: the name and the description are in it, or in the default language when a product is not
// translated into it
export async function getAllProducts(language: string): Promise<Product[]> {
  const response = await useLoadingStore().track(productsApi.getProducts(language));
  return response.data.map(fromApi);
}

// Returns null when the product does not exist (404)
export async function getProduct(id: string, language: string): Promise<Product | null> {
  try {
    const response = await useLoadingStore().track(productsApi.getProduct(id, language));
    return fromApi(response.data);
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    throw error;
  }
}
