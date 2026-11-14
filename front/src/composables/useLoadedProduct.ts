import { computed, onServerPrefetch, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import type { Product } from '@/models/product';
import { getProduct } from '@/services/product.service';
import { useProductHead } from '@/composables/useProductHead';
import { productPath } from '@/utils/productSlug';
import { languageOfPath, localizedPath } from '@/utils/language';

// The product of the page, loaded in the language of the customer, and the address that shows it
export function useLoadedProduct(id: () => string) {
  const { locale } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const product = ref<Product | null>(null);
  const loading = ref(true);
  const failed = ref(false);

  // The one address of the page: the one of the product once it is known
  const canonicalPath = computed(() =>
    product.value ? localizedPath(productPath(product.value), languageOfPath(route.path)) : route.path
  );
  useProductHead(product, canonicalPath);

  // The address without the slug, or with another one (an old name, another language), becomes the canonical one
  function showCanonicalAddress() {
    const isShown = product.value?.id === route.params.id;
    const isInBrowser = typeof window !== 'undefined';
    if (isInBrowser && isShown && route.path !== canonicalPath.value) {
      router.replace({ path: canonicalPath.value, query: route.query });
    }
  }

  async function loadProduct() {
    loading.value = true;
    failed.value = false;
    try {
      product.value = await getProduct(id(), locale.value);
      showCanonicalAddress();
    } catch {
      product.value = null;
      failed.value = true;
    } finally {
      loading.value = false;
    }
  }

  // Reload when navigating from one product to another (e.g. a similar product), or when the customer changes
  // the language: the product is loaded in it only
  const firstLoad = loadProduct();
  watch([id, locale], loadProduct);
  // Pre-rendering waits for the product: the page is then listed with it
  onServerPrefetch(() => firstLoad);

  return { product, loading, failed, loadProduct };
}
