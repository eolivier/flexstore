import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import i18n from '@/plugins/i18n';
import type { AdminProduct, Product, ProductCreation, ProductInput, ProductSave } from '@/models/product';
import { getAllProducts } from '@/services/product.service';
import { useAuthStore } from './auth.store';

export const useProductsStore = defineStore('products', () => {
  const products = ref<Product[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // A SKU free in the catalog, proposed from the category and the name of the product; null when it cannot be
  // proposed
  async function proposeSku(category: string, name: string): Promise<string | null> {
    const query = new URLSearchParams({ category, name });
    const response = await useAuthStore()
      .authFetch(`/api/admin/products/sku-proposal?${query}`)
      .catch(() => null);
    if (!response?.ok) return null;
    const proposal: { sku: string } = await response.json();
    return proposal.sku;
  }

  // The created product keeps the id given by the API: its image and its stock are given with it
  async function addProduct(product: ProductInput): Promise<ProductCreation> {
    return saveProduct('/api/products', 'POST', product);
  }

  // The product with its translations in every language, which the administrator edits; null when it cannot
  // be read
  async function fetchAdminProduct(id: string): Promise<AdminProduct | null> {
    try {
      const response = await useAuthStore().authFetch(`/api/admin/products/${encodeURIComponent(id)}`);
      if (!response.ok) return null;
      return await response.json();
    } catch (e) {
      console.error('Error when reading product', e);
      return null;
    }
  }

  async function updateProduct(id: string, product: ProductInput): Promise<ProductSave> {
    const productSaving = await saveProduct(`/api/products/${id}`, 'PUT', product);
    return productSaving.outcome;
  }

  // Creates (POST) or edits (PUT) the product; 409: another product has its SKU
  async function saveProduct(url: string, method: string, product: ProductInput): Promise<ProductCreation> {
    try {
      const response = await useAuthStore().authFetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
      });
      if (response.status === 409) return { outcome: 'skuAlreadyUsed' };
      if (!response.ok) {
        console.error('Error when saving product', await response.text());
        return { outcome: 'failed' };
      }
      const savedProduct: Product = await response.json();
      await fetchProducts();
      return { outcome: 'saved', productId: savedProduct.id ?? '' };
    } catch (e) {
      console.error('Error when saving product', e);
      return { outcome: 'failed' };
    }
  }

  async function fetchProducts() {
    loading.value = true;
    error.value = null;
    try {
      products.value = await getAllProducts(i18n.global.locale.value);
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error while loading products';
    } finally {
      loading.value = false;
    }
  }

  // The products are loaded in the language of the customer only: they are loaded again when it changes
  watch(
    () => i18n.global.locale.value,
    () => {
      if (products.value.length > 0) fetchProducts();
    }
  );

  return {
    products,
    loading,
    error,
    fetchProducts,
    fetchAdminProduct,
    proposeSku,
    addProduct,
    updateProduct,
  };
});
