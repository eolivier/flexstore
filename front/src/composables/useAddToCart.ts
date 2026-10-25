import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useCartStore } from '@/stores/cart.store';
import type { Item } from '@/models/cart';
import type { Product } from '@/models/product';

export interface CartFeedback {
  show: boolean;
  success: boolean;
  text: string;
}

// Adds a product to the cart (or increments its quantity) and exposes the state for the button and the snackbar
export function useAddToCart() {
  const { t } = useI18n();
  const cartStore = useCartStore();

  const addingProductId = ref<string | undefined>();
  const feedback = ref<CartFeedback>({ show: false, success: true, text: '' });

  async function addToCart(product: Product, quantity = 1) {
    addingProductId.value = product.id;
    const existingItem = cartStore.cartItems?.items.find((i) => i.productId === product.id);
    const item = existingItem
      ? { ...existingItem, productQuantity: existingItem.productQuantity + quantity }
      : productToCartItem(product, quantity);
    const saved = await cartStore.saveItem(item);
    addingProductId.value = undefined;
    feedback.value = {
      show: true,
      success: saved,
      text: saved ? t('products.added', { name: product.name }) : t('products.addError'),
    };
  }

  return { addingProductId, feedback, addToCart };
}

function productToCartItem(product: Product, quantity: number): Item {
  return {
    itemId: '',
    productId: product.id,
    productName: product.name,
    productDescription: product.description,
    productCategory: product.category,
    productPrice: product.price,
    productCurrency: product.currency,
    productQuantity: quantity,
    itemPrice: (product.price ?? 0) * quantity,
  };
}
