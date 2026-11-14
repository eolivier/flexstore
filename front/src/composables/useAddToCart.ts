import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useCartStore } from '@/stores/cart.store';
import { useSnackbar } from './useSnackbar';
import { useAddedToCartSignal } from './useAddedToCartSignal';
import type { Item } from '@/models/cart';
import type { Product } from '@/models/product';

// Adds a product to the cart (or increments its quantity) and tells the result in the snackbar
export function useAddToCart() {
  const { t } = useI18n();
  const cartStore = useCartStore();
  const snackbar = useSnackbar();
  const { announceAdded } = useAddedToCartSignal();

  const addingProductId = ref<string | undefined>();
  const addingAll = ref(false);

  // The cart is reloaded after each save, so the next product finds the quantity already in the cart
  async function saveOneMore(product: Product, quantity = 1): Promise<boolean> {
    const existingItem = cartStore.cartItems?.items.find((i) => i.productId === product.id);
    const item = existingItem
      ? { ...existingItem, productQuantity: existingItem.productQuantity + quantity }
      : productToCartItem(product, quantity);
    const saved = await cartStore.saveItem(item);
    if (saved) announceAdded(product.id);
    return saved;
  }

  // Adds one of each product, one after the other, and tells the result in the snackbar
  async function addAllToCart(products: Product[]) {
    addingAll.value = true;
    let addedCount = 0;
    for (const product of products) {
      if (await saveOneMore(product)) addedCount++;
    }
    addingAll.value = false;
    if (addedCount === products.length) {
      snackbar.success(t('products.addedAll', { count: addedCount }), {
        icon: 'mdi-check-circle-outline',
        action: { text: t('products.viewCart'), to: '/cart' },
      });
    } else snackbar.error(t('products.addAllError'), { icon: 'mdi-alert-circle-outline' });
  }

  async function addToCart(product: Product, quantity = 1) {
    addingProductId.value = product.id;
    const saved = await saveOneMore(product, quantity);
    addingProductId.value = undefined;
    if (saved) {
      snackbar.success(t('products.added', { name: product.name }), {
        icon: 'mdi-check-circle-outline',
        action: { text: t('products.viewCart'), to: '/cart' },
      });
    } else snackbar.error(t('products.addError'), { icon: 'mdi-alert-circle-outline' });
  }

  return { addingProductId, addingAll, addToCart, addAllToCart };
}

function productToCartItem(product: Product, quantity: number): Item {
  return {
    itemId: '',
    productId: product.id,
    productName: product.name,
    productDescription: product.description,
    productCategory: product.category,
    productPrice: product.currentPrice ?? product.price,
    regularPrice: product.price,
    promotionPercentage: product.promotionPercentage,
    productCurrency: product.currency,
    productQuantity: quantity,
    itemPrice: (product.currentPrice ?? product.price ?? 0) * quantity,
  };
}
