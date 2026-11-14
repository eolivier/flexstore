import { ref } from 'vue';

// The last product added to the cart: each addition is a new object, so that watchers see it even for the same product
const lastAddedProduct = ref<{ productId: string | undefined }>();

export function useAddedToCartSignal() {
  function announceAdded(productId: string | undefined) {
    lastAddedProduct.value = { productId };
  }
  return { lastAddedProduct, announceAdded };
}
