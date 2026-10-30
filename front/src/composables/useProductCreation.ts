import type { ProductInput } from '@/models/product';
import { useProductsStore } from '@/stores/products.store';
import { useProductImagesStore } from '@/stores/productImages.store';
import { useStocksStore } from '@/stores/stocks.store';

// How the creation of a product, with its image and its stock, ended:
// - created: the product, its image (if one was chosen) and its stock are saved;
// - partlyCreated: the product is created, but its image or its stock could not be saved (to finish on its
//   page);
// - skuAlreadyUsed, failed: the product is not created
export type ProductCreationOutcome =
  { outcome: 'created' | 'partlyCreated'; productId: string } | { outcome: 'skuAlreadyUsed' | 'failed' };

// The product is created first: its image and its stock are then given to it, with the id it received
export function useProductCreation() {
  const productsStore = useProductsStore();
  const productImagesStore = useProductImagesStore();
  const stocksStore = useStocksStore();

  async function createProduct(product: ProductInput, image: File | null, stock: number) {
    const productCreation = await productsStore.addProduct(product);
    if (productCreation.outcome !== 'saved') return productCreation;
    const { productId } = productCreation;
    const imageUpload = image ? await productImagesStore.uploadImage(productId, image) : 'saved';
    const stockSaved = await stocksStore.changeStock(productId, stock);
    const allSaved = imageUpload === 'saved' && stockSaved;
    const creationOutcome: ProductCreationOutcome = {
      outcome: allSaved ? 'created' : 'partlyCreated',
      productId,
    };
    return creationOutcome;
  }

  return { createProduct };
}
