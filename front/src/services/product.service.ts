import { isAxiosError } from 'axios';
import { productsApi } from '@/api';
import type { Product } from '@/models/product';
import { fromApi } from '@/mappers/product.mapper';
import { useLoadingStore } from '@/stores/loading.store';
import { PAGE_SIZE, totalCountOfAnswer } from '@/utils/pagination';

// language: the name and the description are in it, or in the default language when a product is not
// translated into it
export async function getAllProducts(language: string): Promise<Product[]> {
  const response = await useLoadingStore().track(productsApi.getProducts(language));
  return response.data.map(fromApi);
}

// The products that match, a page at a time, and the number of products that match whatever the page
export interface CatalogPage {
  products: Product[];
  total: number;
}

// What the customer asks of the catalog: the words, the category ('' for all of them), the sort and the page (from 0)
export interface CatalogQuery {
  search: string;
  category: string;
  sort: 'name' | 'priceAsc' | 'priceDesc';
  page: number;
}

// language: the words are searched, and the products read, in it
export async function getCatalogPage(language: string, query: CatalogQuery): Promise<CatalogPage> {
  const { search, category, sort, page } = query;
  const response = await useLoadingStore().track(
    productsApi.getCatalogPage(language, page, PAGE_SIZE, search, category, sort)
  );
  const products = response.data.map(fromApi);
  return { products, total: totalCountOfAnswer(response.headers, products.length) };
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
