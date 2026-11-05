import { computed, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { Product } from '@/models/product';
import { useProductImagesStore } from '@/stores/productImages.store';
import { useReviewsStore } from '@/stores/reviews.store';
import { useStocksStore } from '@/stores/stocks.store';
import { canonicalUrl, shortDescription, SITE_NAME } from '@/utils/seo';
import { productStructuredData } from '@/utils/productStructuredData';
import { usePageHead } from './usePageHead';

// The head of the page of a product: its name and description, its image, and its schema.org data. A product
// that is not found is not indexed
export function useProductHead(product: Ref<Product | null>, path: Ref<string>) {
  const { t } = useI18n();
  const reviewsStore = useReviewsStore();
  const stocksStore = useStocksStore();
  const productImagesStore = useProductImagesStore();

  const imageUrl = computed(() => {
    const id = product.value?.id;
    return id && productImagesStore.hasImage(id) ? productImagesStore.imageUrl(id, 'large') : null;
  });

  usePageHead(() => {
    const found = product.value;
    if (!found) {
      return {
        title: t('products.notFound'),
        description: t('seo.siteDescription'),
        path: path.value,
        indexed: false,
        translated: false,
        imageUrl: null,
        structuredData: null,
      };
    }
    const quantity = stocksStore.quantityOf(found.id);
    return {
      title: found.name ?? SITE_NAME,
      description: shortDescription(found.description),
      path: path.value,
      indexed: true,
      // The slug of the product is in each language its own: the sitemap gives the two addresses
      translated: false,
      imageUrl: imageUrl.value,
      structuredData: productStructuredData({
        product: found,
        url: canonicalUrl(path.value),
        imageUrl: imageUrl.value,
        inStock: quantity === null ? null : quantity > 0,
        rating: reviewsStore.ratingOf(found.id),
      }),
    };
  });
}
