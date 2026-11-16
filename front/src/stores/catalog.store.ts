import { defineStore } from 'pinia';
import { ref } from 'vue';
import i18n from '@/plugins/i18n';
import type { Product } from '@/models/product';
import { getCatalogPage, type CatalogQuery } from '@/services/product.service';

// A page of the catalog that the customer searches, filters and sorts: the server does it, the page shown is the
// one of the last query
export const useCatalogStore = defineStore('catalog', () => {
  const products = ref<Product[]>([]);
  // The number of products that match the query, whatever the page
  const total = ref(0);
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function fetchCatalogPage(query: CatalogQuery) {
    loading.value = true;
    error.value = null;
    try {
      const catalogPage = await getCatalogPage(i18n.global.locale.value, query);
      products.value = catalogPage.products;
      total.value = catalogPage.total;
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error while loading the catalog';
    } finally {
      loading.value = false;
    }
  }

  return { products, total, loading, error, fetchCatalogPage };
});
